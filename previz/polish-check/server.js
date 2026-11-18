// A look at the dark's in-world effects without a game (Fable, 2026-11 polish). Run:  node previz/polish-check/server.js
// then open http://localhost:8767/ and, in the page's console:  check.draw(100)   (a game tick; check.draw(100, true) = 1.0.0's numbers)
//
// It serves check.html, the film's REAL shader files (so the materials are the game's), and one of Codex's game
// screenshots as the sky backdrop. That screenshot is read from the project folder OUTSIDE this repo (..\codex tests\):
// it shows TON 618's own art, so it is never copied into the repo. Without it the backdrop is black.
// check.html mirrors FilmCamera.dark and FilmWorld.dark by hand: after changing a number there, change it here too.
// A POST to /save?name=x with a base64 JPEG body writes x.jpg to the system's temp folder (for looking at a frame).
const http = require('http');
const fs = require('fs');
const os = require('os');
const path = require('path');
const repo = path.join(__dirname, '..', '..');
const shots = path.join(repo, '..', 'codex tests', 'screenshots', '05-Forge-1.21.1');
const film = path.join(repo, 'src', 'main', 'resources', 'assets', 'ton618_addition', 'shaders', 'film');
const routes = {
  '/': [path.join(__dirname, 'check.html'), 'text/html'],
  '/fsh': [path.join(film, 'last_light.fsh'), 'text/plain'],
  '/vsh': [path.join(film, 'last_light.vsh'), 'text/plain'],
  // the film at 3 s (game tick 60, shot 1): the page re-projects it for any other camera
  '/shot3': [path.join(shots, '2026-11-15_13.03.08.png'), 'image/png'],
};
http.createServer((req, res) => {
  if (req.method === 'POST') {
    const chunks = [];
    req.on('data', c => chunks.push(c));
    req.on('end', () => {
      const name = (req.url.split('name=')[1] || 'frame').replace(/[^a-z0-9_-]/gi, '');
      const file = path.join(os.tmpdir(), name + '.jpg');
      fs.writeFileSync(file, Buffer.from(Buffer.concat(chunks).toString(), 'base64'));
      res.writeHead(200);
      res.end('saved ' + file);
    });
    return;
  }
  const r = routes[req.url.split('?')[0]];
  if (!r) { res.writeHead(404); res.end('not here'); return; }
  fs.readFile(r[0], (err, data) => {
    if (err) { res.writeHead(404); res.end(String(err)); return; }
    res.writeHead(200, {'Content-Type': r[1], 'Cache-Control': 'no-store'});
    res.end(data);
  });
}).listen(8767, '127.0.0.1', () => console.log('polish check on http://localhost:8767/'));
