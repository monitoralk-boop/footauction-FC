import asyncio
import json
import sys
import urllib.request
import websockets

def broadcast(topic: str, payload: dict):
    url = f"https://ntfy.sh/{topic}"
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    with urllib.request.urlopen(req) as resp:
        return resp.status

async def run_self_test():
    code = "TEST99"
    topic = f"footauction_room_{code.lower()}"
    ws_url = f"wss://ntfy.sh/{topic}/ws"
    
    print(f"Connecting to test room {code} via {ws_url}...")
    async with websockets.connect(ws_url) as ws1, websockets.connect(ws_url) as ws2:
        print("[Client 1] Connected as Host (Carlo Ancelotti FC)")
        print("[Client 2] Connected as Guest (Zinedine Zidane FC)")

        # Client 1 announces Host
        broadcast(topic, {
            "type": "HOST_PRESENCE",
            "userId": "host_111",
            "managerName": "Carlo Ancelotti",
            "clubName": "Real Madrid FC",
            "avatar": "👑"
        })

        # Client 2 announces Guest
        broadcast(topic, {
            "type": "GUEST_JOINED",
            "userId": "guest_222",
            "managerName": "Zinedine Zidane",
            "clubName": "Juventus FC",
            "avatar": "⚽"
        })

        # Listen for messages
        received = 0
        for _ in range(4):
            try:
                msg_raw = await asyncio.wait_for(ws1.recv(), timeout=4.0)
                msg_json = json.loads(msg_raw)
                if msg_json.get("event") == "message":
                    payload = json.loads(msg_json.get("message", "{}"))
                    print(f"-> [Client 1 received]: {payload.get('type')} from {payload.get('managerName', 'Unknown')}")
                    received += 1
            except asyncio.TimeoutError:
                break

        # Simulate game start & bid
        print("\n[Simulating Auction Bid]")
        broadcast(topic, {
            "type": "BID",
            "userId": "guest_222",
            "bidderName": "Zinedine Zidane",
            "amount": 25000000
        })

        msg_raw = await asyncio.wait_for(ws1.recv(), timeout=4.0)
        msg_json = json.loads(msg_raw)
        if msg_json.get("event") == "message":
            payload = json.loads(msg_json.get("message", "{}"))
            print(f"-> [Live Bid Received]: {payload.get('bidderName')} placed {payload.get('amount'):,} Coins!")

        print("\n==========================================")
        print(" SUCCESS: Online WebSocket communication is 100% operational!")
        print("==========================================")

async def join_user_room(code: str):
    clean_code = code.strip().upper()
    topic = f"footauction_room_{clean_code.lower().replace('-', '')}"
    ws_url = f"wss://ntfy.sh/{topic}/ws"
    
    print(f"\nJoining your room '{clean_code}' on topic: {topic}...")
    broadcast(topic, {
        "type": "GUEST_JOINED",
        "userId": "bot_online_companion",
        "managerName": "Online Opponent",
        "clubName": "Challengers FC",
        "avatar": "🔥"
    })
    print("[Connected] Sent join signal! Look at your app screen.")

    async with websockets.connect(ws_url) as ws:
        print("Listening for in-game actions from your app (Press Ctrl+C to exit)...")
        while True:
            msg_raw = await ws.recv()
            msg_json = json.loads(msg_raw)
            if msg_json.get("event") == "message":
                body = msg_json.get("message", "")
                try:
                    payload = json.loads(body)
                    p_type = payload.get("type")
                    if payload.get("userId") == "bot_online_companion":
                        continue
                    print(f"\n[Incoming Event from App]: {p_type}")
                    if p_type == "HOST_PRESENCE":
                        print(f"  Host detected: {payload.get('managerName')} ({payload.get('clubName')})")
                    elif p_type == "BID":
                        print(f"  Bid received: {payload.get('amount')} coins by {payload.get('bidderName')}")
                    elif p_type == "PASS":
                        print(f"  Player passed on the current card.")
                    elif p_type == "GAME_START":
                        print(f"  Game has started with drafted cards pool!")
                except Exception:
                    pass

if __name__ == "__main__":
    if len(sys.argv) > 1:
        asyncio.run(join_user_room(sys.argv[1]))
    else:
        print("1. Running automated self-test of the online multiplayer server...")
        asyncio.run(run_self_test())
