# FootAuction FC Real-Time Game Server

Free-tier ready Node.js + WebSocket game server and social backend for **FootAuction FC**.

## Free Server & Database Hosting Options ($0 / Month, No Credit Card Required)

### Option 1: Koyeb (Recommended for WebSockets & Zero Sleep)
1. Go to [Koyeb.com](https://www.koyeb.com/) (Sign up with GitHub).
2. Click **Create Service** → Choose **GitHub** → Select your repository (`footauction-fc`).
3. Set the Root Directory to `server`.
4. Koyeb automatically builds and runs `server.js` with WebSockets enabled 24/7 on the Free Nano Tier.
5. Copy your live URL (e.g., `https://footauction-server-yourname.koyeb.app`).

### Option 2: Render.com (1-Click Deployment)
1. Go to [Render.com](https://render.com/) and register for free.
2. Click **New +** → **Web Service** → Connect your GitHub repo.
3. Configure:
   - **Root Directory:** `server`
   - **Build Command:** `npm install`
   - **Start Command:** `node server.js`
   - **Instance Type:** `Free`
4. Deploy! Your API and WebSocket endpoint will be live at `https://your-app.onrender.com`.

### Option 3: Supabase (Free PostgreSQL Database)
1. Go to [Supabase.com](https://supabase.com/) and create a free project.
2. In Project Settings → Database → Copy your `Connection String (URI)`.
3. In Koyeb or Render, set the Environment Variable: `DATABASE_URL=your_supabase_postgres_url`.

---

## Local Development & Testing

```bash
cd server
npm install
npm start
```

Run simulated multiplayer & social tests:
```bash
npm run test-sim
```
