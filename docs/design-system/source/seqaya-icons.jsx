// Custom hand-feel icons for Seqaya. 24×24, 1.5px stroke, rounded caps.
// Not Heroicons, not Lucide — drawn for this specific product.

const Svg = ({ size = 22, children, stroke = 'currentColor', fill = 'none', style }) => (
  <svg width={size} height={size} viewBox="0 0 24 24"
       fill={fill} stroke={stroke} strokeWidth="1.5"
       strokeLinecap="round" strokeLinejoin="round" style={style}>
    {children}
  </svg>
);

const I = {
  leaf: (p) => <Svg {...p}>
    <path d="M4 19c5 0 9-1.5 12-4.5S20 7 20 4c-3 0-7 .8-10 3.8S4 14 4 19z"/>
    <path d="M4 19c3-3 6-5 9-6"/>
  </Svg>,
  drop: (p) => <Svg {...p}>
    <path d="M12 3c-3 4-6 7-6 11a6 6 0 0012 0c0-4-3-7-6-11z"/>
  </Svg>,
  sun: (p) => <Svg {...p}>
    <circle cx="12" cy="12" r="3.5"/>
    <path d="M12 3v2M12 19v2M3 12h2M19 12h2M5.5 5.5l1.4 1.4M17.1 17.1l1.4 1.4M5.5 18.5l1.4-1.4M17.1 6.9l1.4-1.4"/>
  </Svg>,
  thermo: (p) => <Svg {...p}>
    <path d="M13 14V5a2 2 0 10-4 0v9a4 4 0 104 0z"/>
    <circle cx="11" cy="16" r="1.2" fill="currentColor" stroke="none"/>
  </Svg>,
  wind: (p) => <Svg {...p}>
    <path d="M3 9h12a2.5 2.5 0 100-5"/>
    <path d="M3 14h16a3 3 0 110 6"/>
  </Svg>,
  pot: (p) => <Svg {...p}>
    <path d="M6 11h12l-1 9H7l-1-9z"/>
    <path d="M5 11h14"/>
    <path d="M10 11c0-2 1-4 2-6M14 11c0-2-1-3-2-4"/>
  </Svg>,
  phoneTap: (p) => <Svg {...p}>
    <rect x="7" y="3" width="10" height="18" rx="2"/>
    <path d="M10 18h4"/>
  </Svg>,
  device: (p) => <Svg {...p}>
    <rect x="5" y="7" width="14" height="10" rx="2"/>
    <path d="M9 11h6M9 14h4"/>
  </Svg>,
  chart: (p) => <Svg {...p}>
    <path d="M3 17l5-6 4 3 6-8"/>
    <path d="M3 21h18"/>
  </Svg>,
  search: (p) => <Svg {...p}>
    <circle cx="11" cy="11" r="6"/>
    <path d="M20 20l-4.5-4.5"/>
  </Svg>,
  settings: (p) => <Svg {...p}>
    <circle cx="12" cy="12" r="2.5"/>
    <path d="M12 3v2.5M12 18.5V21M3 12h2.5M18.5 12H21M5.8 5.8l1.8 1.8M16.4 16.4l1.8 1.8M5.8 18.2l1.8-1.8M16.4 7.6l1.8-1.8"/>
  </Svg>,
  user: (p) => <Svg {...p}>
    <circle cx="12" cy="8.5" r="3.5"/>
    <path d="M5 20c1-4 4-6 7-6s6 2 7 6"/>
  </Svg>,
  plus: (p) => <Svg {...p}>
    <path d="M12 5v14M5 12h14"/>
  </Svg>,
  close: (p) => <Svg {...p}>
    <path d="M6 6l12 12M18 6L6 18"/>
  </Svg>,
  back: (p) => <Svg {...p}>
    <path d="M15 6l-6 6 6 6"/>
  </Svg>,
  eye: (p) => <Svg {...p}>
    <path d="M2 12s3.5-6.5 10-6.5S22 12 22 12s-3.5 6.5-10 6.5S2 12 2 12z"/>
    <circle cx="12" cy="12" r="2.5"/>
  </Svg>,
  check: (p) => <Svg {...p}>
    <path d="M5 12.5l4.5 4.5L19 7"/>
  </Svg>,
  alert: (p) => <Svg {...p}>
    <path d="M12 4L2 20h20L12 4z"/>
    <path d="M12 10v4M12 17h.01" strokeLinecap="round"/>
  </Svg>,
  trash: (p) => <Svg {...p}>
    <path d="M4 7h16M10 4h4M6 7l1 13h10l1-13"/>
    <path d="M10 11v6M14 11v6"/>
  </Svg>,
  bookmark: (p) => <Svg {...p}>
    <path d="M7 4h10v17l-5-4-5 4V4z"/>
  </Svg>,
  share: (p) => <Svg {...p}>
    <circle cx="6" cy="12" r="2"/>
    <circle cx="18" cy="6" r="2"/>
    <circle cx="18" cy="18" r="2"/>
    <path d="M8 11l8-4M8 13l8 4"/>
  </Svg>,
  more: (p) => <Svg {...p}>
    <circle cx="6" cy="12" r="1.5" fill="currentColor" stroke="none"/>
    <circle cx="12" cy="12" r="1.5" fill="currentColor" stroke="none"/>
    <circle cx="18" cy="12" r="1.5" fill="currentColor" stroke="none"/>
  </Svg>,
  flash: (p) => <Svg {...p}>
    <path d="M13 3L5 14h5l-1 7 8-11h-5l1-7z"/>
  </Svg>,
  flip: (p) => <Svg {...p}>
    <path d="M4 8h11a5 5 0 015 5M20 16H9a5 5 0 01-5-5"/>
    <path d="M7 5L4 8l3 3M17 19l3-3-3-3"/>
  </Svg>,
  gallery: (p) => <Svg {...p}>
    <rect x="3" y="5" width="18" height="14" rx="2"/>
    <circle cx="8" cy="10" r="1.5"/>
    <path d="M21 16l-5-5-8 8"/>
  </Svg>,
  home: (p) => <Svg {...p}>
    <path d="M4 11l8-7 8 7v9H4v-9z"/>
  </Svg>,
  homeFill: (p) => <Svg {...p} fill="currentColor" stroke="currentColor">
    <path d="M4 11l8-7 8 7v9H4v-9z"/>
  </Svg>,
  scan: (p) => <Svg {...p}>
    <path d="M4 8V6a2 2 0 012-2h2M16 4h2a2 2 0 012 2v2M20 16v2a2 2 0 01-2 2h-2M8 20H6a2 2 0 01-2-2v-2"/>
    <circle cx="12" cy="12" r="3"/>
  </Svg>,
  scanFill: (p) => <Svg {...p}>
    <path d="M4 8V6a2 2 0 012-2h2M16 4h2a2 2 0 012 2v2M20 16v2a2 2 0 01-2 2h-2M8 20H6a2 2 0 01-2-2v-2"/>
    <circle cx="12" cy="12" r="3" fill="currentColor" stroke="currentColor"/>
  </Svg>,
  book: (p) => <Svg {...p}>
    <path d="M5 4h7a3 3 0 013 3v13H7a2 2 0 01-2-2V4z"/>
    <path d="M12 7h7v13M15 4v16"/>
  </Svg>,
  bookFill: (p) => <Svg {...p}>
    <path d="M5 4h7a3 3 0 013 3v13H7a2 2 0 01-2-2V4z" fill="currentColor"/>
    <path d="M12 7h7v13M15 4v16"/>
  </Svg>,
};

// A hand-drawn-feel leaf-mark logo (for splash, empty states)
const LeafMark = ({ size = 40, color = '#141413' }) => (
  <svg width={size} height={size} viewBox="0 0 40 40" fill="none"
       stroke={color} strokeWidth="1.25" strokeLinecap="round" strokeLinejoin="round">
    <path d="M6 32c2-11 8-19 20-24-1 13-6 22-20 24z"/>
    <path d="M6 32c5-5 10-9 17-13"/>
    <path d="M12 28c2-1 4-1.5 6-1.5"/>
  </svg>
);

// Quick hand-drawn-looking plant silhouettes for library/empty states.
// Not beautiful — they signal "plant placeholder" without pretending to be real.
const PlantSketch = ({ kind = 'fig', size = 90 }) => {
  const s = { width: size, height: size };
  const stroke = '#3e3d38';
  if (kind === 'fig') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M40 64V30"/>
      <path d="M40 42c-6-1-12-4-14-10 5-1 11 2 14 6z" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M40 36c6-2 12-6 13-13-5 0-11 3-13 7z" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M40 28c-5-3-10-8-9-15 5 1 9 5 10 10z" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M40 50c5-1 10-3 11-9-4-1-9 1-11 5z" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M30 64h20l-2 8H32z" fill="#d97757" fillOpacity="0.12"/>
      <path d="M30 64h20l-2 8H32z"/>
    </svg>
  );
  if (kind === 'monstera') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M40 64V34"/>
      <path d="M40 34c-10-2-18-10-18-22 10 2 16 10 18 20z" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M40 34c10-2 18-10 18-22-10 2-16 10-18 20z" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M27 20l4 5M25 14l4 4M36 10l2 5M44 10l-2 5M49 14l-4 4M53 20l-4 5"/>
      <path d="M30 64h20l-2 8H32z"/>
    </svg>
  );
  if (kind === 'pothos') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M30 64h20l-2 8H32z"/>
      <path d="M40 64c-4-10-14-14-22-12 2 8 10 14 22 12z" fill="#788c5d" fillOpacity="0.2"/>
      <path d="M40 60c4-8 12-12 22-10-2 8-10 12-22 10z" fill="#788c5d" fillOpacity="0.2"/>
      <path d="M40 54c-2-6-8-10-14-8 0 6 6 10 14 8z" fill="#788c5d" fillOpacity="0.2"/>
    </svg>
  );
  if (kind === 'snake') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M30 66l2-50c0-2 3-2 3 0l2 50M36 66l2-46c0-2 3-2 3 0l2 46M42 66l3-40c0-2 3-2 3 0l2 40" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M28 66h24l-2 6H30z"/>
    </svg>
  );
  if (kind === 'basil') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M40 64V38"/>
      <path d="M32 40c-2-4 0-9 3-11 2 3 2 8-3 11zM48 40c2-4 0-9-3-11-2 3-2 8 3 11zM40 34c0-5 3-8 5-9 1 3 1 8-5 9zM40 34c0-5-3-8-5-9-1 3-1 8 5 9z" fill="#788c5d" fillOpacity="0.2"/>
      <path d="M30 64h20l-2 8H32z"/>
    </svg>
  );
  if (kind === 'mint') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M40 64V40M33 44l7 4M47 44l-7 4M30 38l10 4M50 38l-10 4M34 32l6 3M46 32l-6 3" />
      <path d="M30 28c0-4 4-8 10-8s10 4 10 8-4 10-10 10-10-6-10-10z" fill="#788c5d" fillOpacity="0.18"/>
      <path d="M30 64h20l-2 8H32z"/>
    </svg>
  );
  if (kind === 'rosemary') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M40 64V14" />
      <path d="M40 20l-5-3M40 24l5-3M40 30l-6-3M40 36l6-3M40 42l-6-3M40 48l6-3M40 54l-5-2"/>
      <path d="M30 64h20l-2 8H32z"/>
    </svg>
  );
  if (kind === 'succulent') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <ellipse cx="40" cy="38" rx="4" ry="10" fill="#788c5d" fillOpacity="0.2"/>
      <ellipse cx="32" cy="40" rx="4" ry="10" transform="rotate(-30 32 40)" fill="#788c5d" fillOpacity="0.2"/>
      <ellipse cx="48" cy="40" rx="4" ry="10" transform="rotate(30 48 40)" fill="#788c5d" fillOpacity="0.2"/>
      <ellipse cx="28" cy="46" rx="4" ry="9" transform="rotate(-55 28 46)" fill="#788c5d" fillOpacity="0.2"/>
      <ellipse cx="52" cy="46" rx="4" ry="9" transform="rotate(55 52 46)" fill="#788c5d" fillOpacity="0.2"/>
      <path d="M30 56h20l-2 14H32z"/>
    </svg>
  );
  if (kind === 'orchid') return (
    <svg {...s} viewBox="0 0 80 80" fill="none" stroke={stroke} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M40 64V30"/>
      <circle cx="30" cy="22" r="4" fill="#d97757" fillOpacity="0.2"/>
      <circle cx="40" cy="14" r="5" fill="#d97757" fillOpacity="0.2"/>
      <circle cx="50" cy="22" r="4" fill="#d97757" fillOpacity="0.2"/>
      <path d="M28 44l-6-4M52 44l6-4"/>
      <path d="M30 64h20l-2 8H32z"/>
    </svg>
  );
  return null;
};

// Sparkline generator — varies moisture curve by seed
const Sparkline = ({ seed = 1, color = '#788c5d', w = 300, h = 50, waterAt = [] }) => {
  const pts = [];
  let v = 55 + (seed * 7) % 20;
  const n = 48;
  for (let i = 0; i < n; i++) {
    v += (Math.sin(i * 0.4 + seed) * 2) + (Math.cos(i * 0.2 + seed * 2) * 1.5);
    if (waterAt.includes(i)) v = Math.min(80, v + 15);
    v = Math.max(30, Math.min(85, v));
    pts.push([i / (n - 1) * w, h - ((v - 25) / 60) * h]);
  }
  const d = pts.map((p, i) => (i === 0 ? 'M' : 'L') + p[0].toFixed(1) + ',' + p[1].toFixed(1)).join(' ');
  return (
    <svg className="spark" viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="none">
      <path d={d} fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      {waterAt.map((i, k) => {
        const p = pts[i];
        return p ? <circle key={k} cx={p[0]} cy={p[1]} r="2.5" fill="#d97757" /> : null;
      })}
    </svg>
  );
};

Object.assign(window, { I, LeafMark, PlantSketch, Sparkline, Svg });
