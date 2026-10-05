const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
const { Pool } = require('pg');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));

const PORT = process.env.PORT || 3000;
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: '/ws' });

// ==========================================
// SUPABASE POSTGRESQL & MEMORY CACHE
// ==========================================
const DEFAULT_SUPABASE_URL = 'postgresql://postgres.uacilczaagxjhzyrmdhs:Ac18052002%40Ac18052002@aws-0-eu-west-1.pooler.supabase.com:5432/postgres';
const databaseUrl = process.env.DATABASE_URL || DEFAULT_SUPABASE_URL;

let dbPool = null;
if (databaseUrl) {
    dbPool = new Pool({
        connectionString: databaseUrl,
        ssl: { rejectUnauthorized: false }
    });
    console.log('⚡ Initializing Supabase PostgreSQL connection...');
} else {
    console.log('ℹ️ Running in memory mode (Set DATABASE_URL to connect to Supabase).');
}

// In-Memory Real-Time State for low-latency WebSocket gameplay
const users = new Map();
const usersByTag = new Map(); // "managername#1234" -> user
const friendships = new Map(); // userId -> Set<friendUserId>
const pendingFriendRequests = new Map(); // requestId -> { id, fromUserId, toUserId, timestamp, status }
const marketListings = new Map(); // listingId -> { id, sellerId, card, currentBid, buyNowPrice, highestBidderId, expiresAt, status }

// Online WebSockets: userId -> WebSocket
const activeConnections = new Map();
// Active Auction Rooms: roomCode -> { roomCode, hostId, guestId, state, ... }
const auctionRooms = new Map();

function generateTagNumber() {
    return Math.floor(1000 + Math.random() * 9000);
}

// Initialize Supabase Tables (if not created yet) & Hydrate Cache
async function initDatabase() {
    // Seed default system bot
    const botUser = {
        id: 'usr_bot_zidane',
        email: 'zidane@footauction.fc',
        managerName: 'Zinedine Zidane',
        tagNumber: 1998,
        fullTag: 'Zinedine Zidane#1998',
        clubName: 'Icon Legends FC',
        avatarIcon: '👑',
        budget: 150000000,
        trophies: 880,
        divisionTier: 1,
        squad: [],
        reserves: [],
        createdAt: Date.now()
    };
    users.set(botUser.id, botUser);
    usersByTag.set(botUser.fullTag.toLowerCase(), botUser);

    if (!dbPool) return;

    try {
        await dbPool.query(`
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(100) PRIMARY KEY,
                email VARCHAR(255) UNIQUE,
                manager_name VARCHAR(60) NOT NULL,
                tag_number INT NOT NULL,
                full_tag VARCHAR(70) UNIQUE NOT NULL,
                club_name VARCHAR(80) NOT NULL,
                avatar_icon VARCHAR(10) DEFAULT '⚽',
                budget BIGINT DEFAULT 20000000,
                trophies INT DEFAULT 0,
                division_tier INT DEFAULT 9,
                created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
            );

            CREATE TABLE IF NOT EXISTS squads (
                user_id VARCHAR(100) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                starting_11 JSONB DEFAULT '[]'::jsonb,
                reserves JSONB DEFAULT '[]'::jsonb,
                formation VARCHAR(50) DEFAULT '4-3-3 Attack',
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
            );

            CREATE TABLE IF NOT EXISTS friendships (
                id SERIAL PRIMARY KEY,
                user_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
                friend_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
                status VARCHAR(20) DEFAULT 'ACCEPTED',
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                UNIQUE(user_id, friend_id)
            );

            CREATE TABLE IF NOT EXISTS friend_requests (
                request_id VARCHAR(100) PRIMARY KEY,
                from_user_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
                to_user_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
                status VARCHAR(20) DEFAULT 'PENDING',
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
            );

            CREATE TABLE IF NOT EXISTS market_listings (
                id VARCHAR(100) PRIMARY KEY,
                seller_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
                seller_name VARCHAR(60) NOT NULL,
                card_data JSONB NOT NULL,
                starting_bid BIGINT NOT NULL,
                current_bid BIGINT NOT NULL,
                buy_now_price BIGINT NOT NULL,
                highest_bidder_id VARCHAR(100) REFERENCES users(id),
                expires_at BIGINT NOT NULL,
                status VARCHAR(20) DEFAULT 'ACTIVE'
            );
        `);
        console.log('✅ Supabase PostgreSQL schema verified & ready.');

        // Load existing users from Supabase into memory
        const res = await dbPool.query('SELECT * FROM users');
        res.rows.forEach(row => {
            const u = {
                id: row.id,
                email: row.email,
                managerName: row.manager_name,
                tagNumber: row.tag_number,
                fullTag: row.full_tag,
                clubName: row.club_name,
                avatarIcon: row.avatar_icon,
                budget: Number(row.budget),
                trophies: row.trophies,
                divisionTier: row.division_tier,
                squad: [],
                reserves: [],
                createdAt: Number(row.created_at)
            };
            users.set(u.id, u);
            usersByTag.set(u.fullTag.toLowerCase(), u);
        });

        // Load friendships
        const fRes = await dbPool.query('SELECT * FROM friendships');
        fRes.rows.forEach(r => {
            if (!friendships.has(r.user_id)) friendships.set(r.user_id, new Set());
            friendships.get(r.user_id).add(r.friend_id);
        });

        console.log(`[SUPABASE] Hydrated ${users.size} users and friendships into server memory.`);
    } catch (err) {
        console.error('❌ Supabase initialization error:', err.message);
    }
}
initDatabase();

// ==========================================
// REST API ROUTES
// ==========================================

// Health Check
app.get('/api/health', (req, res) => {
    res.json({
        status: 'online',
        service: 'FootAuction FC Master Game Server',
        databaseConnected: !!dbPool,
        connectedPlayers: activeConnections.size,
        totalRegisteredUsers: users.size,
        activeAuctions: auctionRooms.size,
        timestamp: Date.now()
    });
});

// 1. REGISTER
app.post('/api/auth/register', async (req, res) => {
    const { email, managerName, clubName, avatarIcon } = req.body;

    if (!managerName || !clubName) {
        return res.status(400).json({ error: 'Manager name and club name are required.' });
    }

    const cleanName = managerName.trim();
    let tag = generateTagNumber();
    let fullTag = `${cleanName}#${tag}`;

    let attempts = 0;
    while (usersByTag.has(fullTag.toLowerCase()) && attempts < 20) {
        tag = generateTagNumber();
        fullTag = `${cleanName}#${tag}`;
        attempts++;
    }

    const userId = `usr_${Date.now()}_${tag}`;
    const newUser = {
        id: userId,
        email: email || `${userId}@footauction.local`,
        managerName: cleanName,
        tagNumber: tag,
        fullTag: fullTag,
        clubName: clubName.trim(),
        avatarIcon: avatarIcon || '⚽',
        budget: 20000000,
        trophies: 0,
        divisionTier: 9,
        squad: [],
        reserves: [],
        createdAt: Date.now()
    };

    users.set(userId, newUser);
    usersByTag.set(fullTag.toLowerCase(), newUser);
    friendships.set(userId, new Set());

    // Auto-befriend Zidane bot
    friendships.get(userId).add('usr_bot_zidane');
    if (!friendships.has('usr_bot_zidane')) friendships.set('usr_bot_zidane', new Set());
    friendships.get('usr_bot_zidane').add(userId);

    // Save to Supabase PostgreSQL
    if (dbPool) {
        try {
            await dbPool.query(
                `INSERT INTO users (id, email, manager_name, tag_number, full_tag, club_name, avatar_icon, budget, trophies, division_tier)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                 ON CONFLICT (id) DO NOTHING`,
                [newUser.id, newUser.email, newUser.managerName, newUser.tagNumber, newUser.fullTag, newUser.clubName, newUser.avatarIcon, newUser.budget, newUser.trophies, newUser.divisionTier]
            );
            await dbPool.query(
                `INSERT INTO friendships (user_id, friend_id) VALUES ($1, 'usr_bot_zidane'), ('usr_bot_zidane', $1) ON CONFLICT DO NOTHING`,
                [newUser.id]
            );
        } catch (dbErr) {
            console.error('[SUPABASE WRITE ERROR]', dbErr.message);
        }
    }

    console.log(`[REGISTER] New Manager saved to Supabase: ${fullTag}`);

    res.json({ success: true, user: newUser });
});

// 2. LOGIN (BY TAG OR ID)
app.post('/api/auth/login', async (req, res) => {
    const { identifier } = req.body;
    if (!identifier) return res.status(400).json({ error: 'Tag or ID required' });

    const clean = identifier.trim().toLowerCase();
    let user = usersByTag.get(clean) || users.get(identifier.trim());

    // If not in cache, check Supabase
    if (!user && dbPool) {
        try {
            const q = await dbPool.query('SELECT * FROM users WHERE LOWER(full_tag) = $1 OR id = $2', [clean, identifier.trim()]);
            if (q.rows.length > 0) {
                const r = q.rows[0];
                user = {
                    id: r.id,
                    email: r.email,
                    managerName: r.manager_name,
                    tagNumber: r.tag_number,
                    fullTag: r.full_tag,
                    clubName: r.club_name,
                    avatarIcon: r.avatar_icon,
                    budget: Number(r.budget),
                    trophies: r.trophies,
                    divisionTier: r.division_tier,
                    squad: [],
                    reserves: [],
                    createdAt: Number(r.created_at)
                };
                users.set(user.id, user);
                usersByTag.set(user.fullTag.toLowerCase(), user);
            }
        } catch (err) {
            console.error('[LOGIN DB QUERY ERROR]', err.message);
        }
    }

    if (!user) return res.status(404).json({ error: 'User not found. Check your Manager Tag.' });

    res.json({ success: true, user });
});

// 3. GET PROFILE
app.get('/api/profile/:userId', (req, res) => {
    const user = users.get(req.params.userId);
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({ success: true, user });
});

// 4. SQUAD SYNC (PERSISTED IN SUPABASE)
app.post('/api/squad/sync', async (req, res) => {
    const { userId, squad, reserves, formation } = req.body;
    const user = users.get(userId);
    if (!user) return res.status(404).json({ error: 'User not found' });

    if (Array.isArray(squad)) user.squad = squad;
    if (Array.isArray(reserves)) user.reserves = reserves;
    if (formation) user.formation = formation;

    if (dbPool) {
        try {
            await dbPool.query(
                `INSERT INTO squads (user_id, starting_11, reserves, formation)
                 VALUES ($1, $2, $3, $4)
                 ON CONFLICT (user_id) DO UPDATE SET starting_11 = $2, reserves = $3, formation = $4, updated_at = NOW()`,
                [userId, JSON.stringify(user.squad), JSON.stringify(user.reserves), user.formation || '4-3-3 Attack']
            );
        } catch (dbErr) {
            console.error('[SUPABASE SQUAD SYNC ERROR]', dbErr.message);
        }
    }

    res.json({ success: true, message: 'Squad synchronized with Supabase database.' });
});

app.get('/api/squad/:userId', async (req, res) => {
    const user = users.get(req.params.userId);

    // If squad empty in memory, check Supabase
    if (dbPool && (!user || user.squad.length === 0)) {
        try {
            const sqRes = await dbPool.query('SELECT * FROM squads WHERE user_id = $1', [req.params.userId]);
            if (sqRes.rows.length > 0) {
                const row = sqRes.rows[0];
                return res.json({
                    success: true,
                    squad: row.starting_11,
                    reserves: row.reserves,
                    formation: row.formation
                });
            }
        } catch (_) {}
    }

    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({
        success: true,
        squad: user.squad,
        reserves: user.reserves,
        formation: user.formation || '4-3-3 Attack'
    });
});

// 5. FRIENDS & SOCIAL SYSTEM
app.get('/api/friends/list/:userId', (req, res) => {
    const { userId } = req.params;
    const user = users.get(userId);
    if (!user) return res.status(404).json({ error: 'User not found' });

    const userFriends = friendships.get(userId) || new Set();
    const friendsList = Array.from(userFriends).map(friendId => {
        const friend = users.get(friendId);
        if (!friend) return null;
        const isOnline = activeConnections.has(friendId);
        return {
            userId: friend.id,
            managerName: friend.managerName,
            managerTag: friend.fullTag,
            clubName: friend.clubName,
            avatarIcon: friend.avatarIcon,
            trophies: friend.trophies,
            divisionTier: friend.divisionTier,
            isOnline: isOnline
        };
    }).filter(Boolean);

    res.json({ success: true, friends: friendsList });
});

app.post('/api/friends/request', (req, res) => {
    const { fromUserId, targetTag } = req.body;
    const fromUser = users.get(fromUserId);
    if (!fromUser) return res.status(404).json({ error: 'Sender not found' });
    if (!targetTag) return res.status(400).json({ error: 'Target tag required' });

    const cleanTargetTag = targetTag.trim().toLowerCase();
    const targetUser = usersByTag.get(cleanTargetTag);

    if (!targetUser) return res.status(404).json({ error: `Manager with tag '${targetTag}' was not found.` });
    if (targetUser.id === fromUserId) return res.status(400).json({ error: 'You cannot add yourself.' });

    const userFriends = friendships.get(fromUserId) || new Set();
    if (userFriends.has(targetUser.id)) return res.status(400).json({ error: `${targetUser.managerName} is already your friend!` });

    const requestId = `freq_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const request = {
        requestId,
        fromUserId,
        fromManagerName: fromUser.managerName,
        fromTag: fromUser.fullTag,
        fromClub: fromUser.clubName,
        fromAvatar: fromUser.avatarIcon,
        toUserId: targetUser.id,
        timestamp: Date.now(),
        status: 'PENDING'
    };

    pendingFriendRequests.set(requestId, request);

    if (dbPool) {
        dbPool.query(
            `INSERT INTO friend_requests (request_id, from_user_id, to_user_id, status) VALUES ($1, $2, $3, $4)`,
            [requestId, fromUserId, targetUser.id, 'PENDING']
        ).catch(() => {});
    }

    const targetSocket = activeConnections.get(targetUser.id);
    if (targetSocket && targetSocket.readyState === WebSocket.OPEN) {
        targetSocket.send(JSON.stringify({ event: 'FRIEND_REQUEST_RECEIVED', request }));
    }

    res.json({ success: true, message: `Friend request sent to ${targetUser.fullTag}!` });
});

app.get('/api/friends/pending/:userId', (req, res) => {
    const { userId } = req.params;
    const incoming = [];
    for (const reqItem of pendingFriendRequests.values()) {
        if (reqItem.toUserId === userId && reqItem.status === 'PENDING') {
            incoming.push(reqItem);
        }
    }
    res.json({ success: true, requests: incoming });
});

app.post('/api/friends/respond', async (req, res) => {
    const { requestId, action } = req.body;
    const request = pendingFriendRequests.get(requestId);
    if (!request) return res.status(404).json({ error: 'Request not found' });

    if (action === 'ACCEPT') {
        request.status = 'ACCEPTED';
        if (!friendships.has(request.fromUserId)) friendships.set(request.fromUserId, new Set());
        if (!friendships.has(request.toUserId)) friendships.set(request.toUserId, new Set());

        friendships.get(request.fromUserId).add(request.toUserId);
        friendships.get(request.toUserId).add(request.fromUserId);

        if (dbPool) {
            try {
                await dbPool.query(
                    `INSERT INTO friendships (user_id, friend_id) VALUES ($1, $2), ($2, $1) ON CONFLICT DO NOTHING`,
                    [request.fromUserId, request.toUserId]
                );
                await dbPool.query('DELETE FROM friend_requests WHERE request_id = $1', [requestId]);
            } catch (_) {}
        }

        const s1 = activeConnections.get(request.fromUserId);
        const s2 = activeConnections.get(request.toUserId);
        const acceptedMsg = JSON.stringify({ event: 'FRIEND_ACCEPTED', requestId });
        if (s1 && s1.readyState === WebSocket.OPEN) s1.send(acceptedMsg);
        if (s2 && s2.readyState === WebSocket.OPEN) s2.send(acceptedMsg);

        pendingFriendRequests.delete(requestId);
        return res.json({ success: true, message: 'Friend request accepted and saved to Supabase!' });
    } else {
        request.status = 'DECLINED';
        if (dbPool) dbPool.query('DELETE FROM friend_requests WHERE request_id = $1', [requestId]).catch(() => {});
        pendingFriendRequests.delete(requestId);
        return res.json({ success: true, message: 'Friend request declined.' });
    }
});

// 6. GLOBAL TRANSFER MARKET
app.get('/api/market/listings', (req, res) => {
    const now = Date.now();
    const active = [];
    for (const listing of marketListings.values()) {
        if (listing.status === 'ACTIVE' && listing.expiresAt > now) {
            active.push(listing);
        }
    }
    res.json({ success: true, listings: active });
});

app.post('/api/market/list-card', async (req, res) => {
    const { sellerId, card, startingBid, buyNowPrice, durationMinutes } = req.body;
    const user = users.get(sellerId);
    if (!user) return res.status(404).json({ error: 'User not found' });

    const listingId = `mkt_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
    const expiresAt = Date.now() + ((durationMinutes || 15) * 60 * 1000);

    const listing = {
        id: listingId,
        sellerId,
        sellerName: user.managerName,
        card,
        startingBid: startingBid || 10000000,
        currentBid: startingBid || 10000000,
        buyNowPrice: buyNowPrice || 35000000,
        highestBidderId: null,
        highestBidderName: null,
        bidCount: 0,
        expiresAt,
        status: 'ACTIVE'
    };

    marketListings.set(listingId, listing);

    if (dbPool) {
        try {
            await dbPool.query(
                `INSERT INTO market_listings (id, seller_id, seller_name, card_data, starting_bid, current_bid, buy_now_price, expires_at, status)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)`,
                [listing.id, listing.sellerId, listing.sellerName, JSON.stringify(listing.card), listing.startingBid, listing.currentBid, listing.buyNowPrice, listing.expiresAt, listing.status]
            );
        } catch (_) {}
    }

    broadcastAll({ event: 'MARKET_NEW_LISTING', listing });

    res.json({ success: true, listing });
});

// ==========================================
// WEBSOCKET REAL-TIME SERVER
// ==========================================
function broadcastAll(payload) {
    const msg = JSON.stringify(payload);
    for (const client of activeConnections.values()) {
        if (client.readyState === WebSocket.OPEN) {
            client.send(msg);
        }
    }
}

wss.on('connection', (ws) => {
    let currentUserId = null;

    ws.on('message', (messageRaw) => {
        try {
            const data = JSON.parse(messageRaw);
            const { event } = data;

            switch (event) {
                case 'REGISTER_PRESENCE': {
                    currentUserId = data.userId;
                    activeConnections.set(currentUserId, ws);

                    const myFriends = friendships.get(currentUserId) || new Set();
                    myFriends.forEach(friendId => {
                        const friendSocket = activeConnections.get(friendId);
                        if (friendSocket && friendSocket.readyState === WebSocket.OPEN) {
                            friendSocket.send(JSON.stringify({
                                event: 'FRIEND_STATUS_UPDATE',
                                userId: currentUserId,
                                isOnline: true
                            }));
                        }
                    });

                    ws.send(JSON.stringify({
                        event: 'PRESENCE_CONFIRMED',
                        userId: currentUserId,
                        serverTime: Date.now()
                    }));
                    break;
                }

                case 'SEND_CHALLENGE': {
                    const { fromUserId, targetUserId, mode } = data;
                    const fromUser = users.get(fromUserId);
                    const targetSocket = activeConnections.get(targetUserId);

                    if (!targetSocket || targetSocket.readyState !== WebSocket.OPEN) {
                        return ws.send(JSON.stringify({
                            event: 'CHALLENGE_ERROR',
                            message: 'Friend is currently offline.'
                        }));
                    }

                    const challengeId = `chlg_${Date.now()}`;
                    targetSocket.send(JSON.stringify({
                        event: 'CHALLENGE_INVITE',
                        challengeId,
                        fromUserId,
                        fromManagerName: fromUser ? fromUser.managerName : 'Friend',
                        fromAvatar: fromUser ? fromUser.avatarIcon : '⚽',
                        fromClub: fromUser ? fromUser.clubName : 'Rival FC',
                        mode: mode || 'DUEL'
                    }));

                    ws.send(JSON.stringify({ event: 'CHALLENGE_SENT', targetUserId }));
                    break;
                }

                case 'ACCEPT_CHALLENGE': {
                    const { challengeId, hostId, guestId } = data;
                    const roomCode = `FA-${Math.floor(1000 + Math.random() * 9000)}`;

                    const hostSocket = activeConnections.get(hostId);
                    const guestSocket = activeConnections.get(guestId);

                    const roomPayload = {
                        event: 'CHALLENGE_ROOM_READY',
                        challengeId,
                        roomCode,
                        hostId,
                        guestId
                    };

                    if (hostSocket && hostSocket.readyState === WebSocket.OPEN) hostSocket.send(JSON.stringify(roomPayload));
                    if (guestSocket && guestSocket.readyState === WebSocket.OPEN) guestSocket.send(JSON.stringify(roomPayload));
                    break;
                }

                case 'AUCTION_BID': {
                    const { roomCode, userId, amount } = data;
                    const room = auctionRooms.get(roomCode);
                    if (!room) return;

                    const user = users.get(userId);
                    if (!user || user.budget < amount) {
                        return ws.send(JSON.stringify({
                            event: 'BID_REJECTED',
                            reason: 'Insufficient funds.'
                        }));
                    }

                    if (amount <= room.currentHighestBid) {
                        return ws.send(JSON.stringify({
                            event: 'BID_REJECTED',
                            reason: 'Bid must be higher than current highest bid.'
                        }));
                    }

                    room.currentHighestBid = amount;
                    room.highestBidderId = userId;
                    room.highestBidderName = user.managerName;
                    room.secondsRemaining = 10;

                    broadcastToRoom(roomCode, {
                        event: 'AUCTION_BID_ACCEPTED',
                        highestBid: amount,
                        bidderId: userId,
                        bidderName: user.managerName,
                        secondsRemaining: 10
                    });
                    break;
                }
            }
        } catch (err) {
            console.error('[WS ERROR]', err.message);
        }
    });

    ws.on('close', () => {
        if (currentUserId) {
            activeConnections.delete(currentUserId);
            const myFriends = friendships.get(currentUserId) || new Set();
            myFriends.forEach(friendId => {
                const friendSocket = activeConnections.get(friendId);
                if (friendSocket && friendSocket.readyState === WebSocket.OPEN) {
                    friendSocket.send(JSON.stringify({
                        event: 'FRIEND_STATUS_UPDATE',
                        userId: currentUserId,
                        isOnline: false
                    }));
                }
            });
        }
    });
});

function broadcastToRoom(roomCode, payload) {
    const room = auctionRooms.get(roomCode);
    if (!room) return;
    const msg = JSON.stringify(payload);
    [room.hostId, room.guestId].forEach(uid => {
        const socket = activeConnections.get(uid);
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(msg);
        }
    });
}

// Start Server
server.listen(PORT, '0.0.0.0', () => {
    console.log(`====================================================`);
    console.log(`🚀 FootAuction FC Real-Time Server running on port ${PORT}`);
    console.log(`📡 WebSocket Gateway ready at ws://localhost:${PORT}/ws`);
    console.log(`⚡ Supabase PostgreSQL Ready (Auto-creates tables on boot)`);
    console.log(`====================================================`);
});
