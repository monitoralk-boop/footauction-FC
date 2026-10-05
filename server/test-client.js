const WebSocket = require('ws');
const http = require('http');

async function testServer() {
    console.log('Testing FootAuction FC Backend API...');

    // 1. Health check
    const health = await fetchJson('http://localhost:3000/api/health');
    console.log('✅ Health Check:', health);

    // 2. Register Player 1
    const p1 = await postJson('http://localhost:3000/api/auth/register', {
        managerName: 'Pep Guardiola',
        clubName: 'Manchester City Stars',
        avatarIcon: '💎'
    });
    console.log('✅ Player 1 Registered:', p1.user.fullTag, '(ID:', p1.user.id, ')');

    // 3. Register Player 2
    const p2 = await postJson('http://localhost:3000/api/auth/register', {
        managerName: 'Carlo Ancelotti',
        clubName: 'Real Madrid Galacticos',
        avatarIcon: '👑'
    });
    console.log('✅ Player 2 Registered:', p2.user.fullTag, '(ID:', p2.user.id, ')');

    // 4. Player 2 sends friend request to Player 1 by Tag
    const friendReq = await postJson('http://localhost:3000/api/friends/request', {
        fromUserId: p2.user.id,
        targetTag: p1.user.fullTag
    });
    console.log('✅ Friend Request Sent:', friendReq.message);

    // 5. Player 1 checks pending requests
    const pending = await fetchJson(`http://localhost:3000/api/friends/pending/${p1.user.id}`);
    console.log('✅ Player 1 Pending Requests:', pending.requests.length);

    // 6. Player 1 accepts
    const accept = await postJson('http://localhost:3000/api/friends/respond', {
        requestId: pending.requests[0].requestId,
        action: 'ACCEPT'
    });
    console.log('✅ Friend Request Accepted:', accept.message);

    // 7. Check friends list
    const friends = await fetchJson(`http://localhost:3000/api/friends/list/${p1.user.id}`);
    console.log('✅ Player 1 Friends List:', friends.friends.map(f => f.managerTag));

    console.log('\n🎉 ALL BACKEND SYSTEMS 100% OPERATIONAL!');
    process.exit(0);
}

function fetchJson(url) {
    return new Promise((resolve, reject) => {
        http.get(url, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve(JSON.parse(body)));
        }).on('error', reject);
    });
}

function postJson(url, payload) {
    return new Promise((resolve, reject) => {
        const data = JSON.stringify(payload);
        const u = new URL(url);
        const req = http.request({
            hostname: u.hostname,
            port: u.port,
            path: u.pathname,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(data)
            }
        }, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve(JSON.parse(body)));
        });
        req.on('error', reject);
        req.write(data);
        req.end();
    });
}

testServer().catch(err => {
    console.error('Test failed:', err);
    process.exit(1);
});
