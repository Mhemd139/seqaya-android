import { writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';

const SIZE = 180;
const TARGET_ALPHA = Math.round(0.02 * 255);

function mulberry32(seed) {
  return () => {
    seed = (seed + 0x6d2b79f5) | 0;
    let t = seed;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function crc32(buf) {
  let c, table = [];
  for (let n = 0; n < 256; n++) {
    c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    table[n] = c;
  }
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) crc = (crc >>> 8) ^ table[(crc ^ buf[i]) & 0xff];
  return (crc ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const typeBuf = Buffer.from(type, 'ascii');
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(Buffer.concat([typeBuf, data])), 0);
  return Buffer.concat([len, typeBuf, data, crc]);
}

const rand = mulberry32(0x5e9a1a);
const raw = Buffer.alloc(SIZE * (1 + SIZE * 2));

for (let y = 0; y < SIZE; y++) {
  const rowStart = y * (1 + SIZE * 2);
  raw[rowStart] = 0;
  for (let x = 0; x < SIZE; x++) {
    const n = 0.5 * rand() + 0.3 * rand() + 0.2 * rand();
    const gray = Math.round(20 + n * 25);
    const pixelOffset = rowStart + 1 + x * 2;
    raw[pixelOffset] = gray;
    raw[pixelOffset + 1] = TARGET_ALPHA;
  }
}

const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(SIZE, 0);
ihdr.writeUInt32BE(SIZE, 4);
ihdr[8] = 8;
ihdr[9] = 4;
ihdr[10] = 0;
ihdr[11] = 0;
ihdr[12] = 0;

const png = Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
  chunk('IHDR', ihdr),
  chunk('IDAT', deflateSync(raw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]);

const out = process.argv[2] ?? 'paper_grain.png';
writeFileSync(out, png);
console.log(`Wrote ${out} (${png.length} bytes, ${SIZE}x${SIZE}, alpha ${TARGET_ALPHA}/255)`);
