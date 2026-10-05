const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));

const PORT = process.env.PORT || 3000;
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: '/ws' });

// ==========================================
// IN-MEMORY GAME DATABASE (READY FOR SUPABASE/POSTGRES)
// ==========================================
// User structure:
// { id, email, managerName, tagNumber, fullTag, clubName, avatarIcon, budget, trophies, divisionTier, squad, reserves, createdAt }
const users = new Map();
const usersByTag = new Map(); // "managername#1234" -> user
const friendships = new Map(); // userId -> Set<friendUserId>
const pendingFriendRequests = new Map(); // requestId -> { id, fromUserId, toUserId, timestamp, status }
const marketListings = new Map(); // listingId -> { id, sellerId, card, currentBid, buyNowPrice, highestBidderId, expiresAt, status }

// Online WebSockets map: userId -> WebSocket
const activeConnections = new Map();
// Active Auction Rooms: roomCode -> { roomCode, hostId, guestId, state, timerJob, ... }
const auctionRooms = new Map();

// Helper: Generate random 4-digit tag
function generateTagNumber() {
    return Math.floor(1000 + Math.random() * 9000);
}

// Seed initial system bots/mock users for transfer market & friends test
function seedDefaultData() {
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
}
seedDefaultData();

// ==========================================
// REST API ROUTES
// ==========================================

// Health Check
app.get('/api/health', (req, res) => {
    res.json({
        status: 'online',
        service: 'FootAuction FC Master Game Server',
        connectedPlayers: activeConnections.size,
        totalRegisteredUsers: users.size,
        activeAuctions: auctionRooms.size,
        timestamp: Date.now()
    });
});

// 1. REGISTER / ACCOUNT CREATION
app.post('/api/auth/register', (req, res) => {
    const { email, managerName, clubName, avatarIcon } = req.body;

    if (!managerName || !clubName) {
        return res.status(400).json({ error: 'Manager name and club name are required.' });
    }

    const cleanName = managerName.trim();
    let tag = generateTagNumber();
    let fullTag = `${cleanName}#${tag}`;

    // Ensure unique tag
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
        budget: 20000000, // Starting €20M
        trophies: 0,
        divisionTier: 9,
        squad: [],
        reserves: [],
        createdAt: Date.now()
    };

    users.set(userId, newUser);
    usersByTag.set(fullTag.toLowerCase(), newUser);
    friendships.set(userId, new Set());

    // Auto-befriend the Zidane bot as welcoming friend!
    friendships.get(userId).add('usr_bot_zidane');
    if (!friendships.has('usr_bot_zidane')) friendships.set('usr_bot_zidane', new Set());
    friendships.get('usr_bot_zidane').add(userId);

    console.log(`[REGISTER] New Manager: ${fullTag} (Club: ${newUser.clubName})`);

    res.json({
        success: true,
        user: newUser
    });
});

// 2. LOGIN (BY TAG OR ID)
app.post('/api/auth/login', (req, res) => {
    const { identifier } = req.body; // Can be fullTag (e.g. "Coach#1234") or userId
    if (!identifier) {
        return res.status(400).json({ error: 'Tag or ID required' });
    }

    const clean = identifier.trim().toLowerCase();
    const user = usersByTag.get(clean) || users.get(identifier.trim());

    if (!user) {
        return res.status(404).json({ error: 'User not found. Check your Manager Tag.' });
    }

    res.json({
        success: true,
        user
    });
});

// 3. GET PROFILE
app.get('/api/profile/:userId', (req, res) => {
    const user = users.get(req.params.userId);
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({ success: true, user });
});

// 4. SQUAD SYNC (SAVE / LOAD)
app.post('/api/squad/sync', (req, res) => {
    const { userId, squad, reserves, formation } = req.body;
    const user = users.get(userId);
    if (!user) return res.status(404).json({ error: 'User not found' });

    if (Array.isArray(squad)) user.squad = squad;
    if (Array.isArray(reserves)) user.reserves = reserves;
    if (formation) user.formation = formation;

    res.json({ success: true, message: 'Squad synchronized with server.' });
});

app.get('/api/squad/:userId', (req, res) => {
    const user = users.get(req.params.userId);
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({
        success: true,
        squad: user.squad,
        reserves: user.reserves,
        formation: user.formation || '4-3-3 Attack'
    });
});

// 5. FRIENDS & SOCIAL SYSTEM
// Get Friends List
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

// Send Friend Request by Tag
app.post('/api/friends/request', (req, res) => {
    const { fromUserId, targetTag } = req.body;
    const fromUser = users.get(fromUserId);
    if (!fromUser) return res.status(404).json({ error: 'Sender not found' });

    if (!targetTag) return res.status(400).json({ error: 'Target tag required' });

    const cleanTargetTag = targetTag.trim().toLowerCase();
    const targetUser = usersByTag.get(cleanTargetTag);

    if (!targetUser) {
        return res.status(404).json({ error: `Manager with tag '${targetTag}' was not found.` });
    }

    if (targetUser.id === fromUserId) {
        return res.status(400).json({ error: 'You cannot add yourself as a friend.' });
    }

    const userFriends = friendships.get(fromUserId) || new Set();
    if (userFriends.has(targetUser.id)) {
        return res.status(400).json({ error: `${targetUser.managerName} is already your friend!` });
    }

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

    // Notify target user via WebSocket if online!
    const targetSocket = activeConnections.get(targetUser.id);
    if (targetSocket && targetSocket.readyState === WebSocket.OPEN) {
        targetSocket.send(JSON.stringify({
            event: 'FRIEND_REQUEST_RECEIVED',
            request
        }));
    }

    console.log(`[FRIEND REQUEST] ${fromUser.fullTag} -> ${targetUser.fullTag}`);
    res.json({ success: true, message: `Friend request sent to ${targetUser.fullTag}!` });
});

// Get Pending Incoming Requests
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

// Respond to Friend Request (ACCEPT / DECLINE)
app.post('/api/friends/respond', (req, res) => {
    const { requestId, action } = req.body; // action: 'ACCEPT' or 'DECLINE'
    const request = pendingFriendRequests.get(requestId);

    if (!request) return res.status(404).json({ error: 'Request not found' });

    if (action === 'ACCEPT') {
        request.status = 'ACCEPTED';
        if (!friendships.has(request.fromUserId)) friendships.set(request.fromUserId, new Set());
        if (!friendships.has(request.toUserId)) friendships.set(request.toUserId, new Set());

        friendships.get(request.fromUserId).add(request.toUserId);
        friendships.get(request.toUserId).add(request.fromUserId);

        // Notify both if online
        const s1 = activeConnections.get(request.fromUserId);
        const s2 = activeConnections.get(request.toUserId);
        const acceptedMsg = JSON.stringify({ event: 'FRIEND_ACCEPTED', requestId });
        if (s1 && s1.readyState === WebSocket.OPEN) s1.send(acceptedMsg);
        if (s2 && s2.readyState === WebSocket.OPEN) s2.send(acceptedMsg);

        pendingFriendRequests.delete(requestId);
        return res.json({ success: true, message: 'Friend request accepted!' });
    } else {
        request.status = 'DECLINED';
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

app.post('/api/market/list-card', (req, res) => {
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

    // Broadcast new listing to all connected players
    broadcastAll({
        event: 'MARKET_NEW_LISTING',
        listing
    });

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
                // Presence registration
                case 'REGISTER_PRESENCE': {
                    currentUserId = data.userId;
                    activeConnections.set(currentUserId, ws);
                    console.log(`[ONLINE] Player connected: ${currentUserId}`);

                    // Notify friends that this user is now online
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

                // Direct Challenge
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

                    ws.send(JSON.stringify({
                        event: 'CHALLENGE_SENT',
                        targetUserId
                    }));
                    break;
                }

                // Accept Challenge -> Auto-create Room
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

                // Real-time Authoritative Auction Bidding
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
                    room.secondsRemaining = 10; // Server resets 10s timer!

                    // Broadcast new bid to all participants in this room
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
            console.log(`[OFFLINE] Player disconnected: ${currentUserId}`);

            // Notify friends that user is offline
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
    console.log(`⚽ Free & Serverless Ready (Render / Koyeb / Supabase)`);
    console.log(`====================================================`);
});
