// C1 Camera, C2 Analyzing, C3 Result, C4 No match

const C1_Camera = () => (
  <Screen id="C · 01" name="Scan · camera">
    <Phone tab="scan">
      <div style={{ position: 'absolute', inset: 0, background: '#2a2f26' }}>
        <svg width="100%" height="100%" viewBox="0 0 360 700" preserveAspectRatio="xMidYMid slice" style={{ position: 'absolute', inset: 0 }}>
          <defs>
            <radialGradient id="cam" cx="50%" cy="45%" r="60%">
              <stop offset="0%" stopColor="#4a5240"/>
              <stop offset="100%" stopColor="#1a1f16"/>
            </radialGradient>
          </defs>
          <rect width="360" height="700" fill="url(#cam)"/>
          {/* suggestive leaf silhouettes */}
          <path d="M60 420c40-20 80-60 90-160-60 20-90 80-90 160z" fill="#566148" opacity="0.8"/>
          <path d="M200 460c50-30 90-80 100-180-70 10-100 90-100 180z" fill="#445138" opacity="0.75"/>
        </svg>
      </div>

      <div style={{ position: 'relative', zIndex: 2, color: 'var(--bg-cream)', height: '100%', display: 'flex', flexDirection: 'column' }}>
        <div style={{ background: 'rgba(250,249,245,0.12)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '14px 18px' }}>
          <I.back size={22}/>
          <span style={{ fontFamily: 'var(--serif)', fontSize: 16, fontWeight: 500 }}>Identify</span>
          <I.flash size={20}/>
        </div>

        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 20 }}>
          <div style={{ fontFamily: 'var(--serif)', fontStyle: 'italic', fontSize: 14, opacity: 0.9 }}>
            Center the leaf in the frame.
          </div>
          <div style={{ position: 'relative', width: 260, height: 260 }}>
            {['top-left', 'top-right', 'bottom-left', 'bottom-right'].map(pos => {
              const t = pos.includes('top'), l = pos.includes('left');
              return (
                <div key={pos} style={{
                  position: 'absolute',
                  [t ? 'top' : 'bottom']: 0,
                  [l ? 'left' : 'right']: 0,
                  width: 28, height: 28,
                  borderTop: t ? '2px solid var(--accent-brown)' : 'none',
                  borderBottom: !t ? '2px solid var(--accent-brown)' : 'none',
                  borderLeft: l ? '2px solid var(--accent-brown)' : 'none',
                  borderRight: !l ? '2px solid var(--accent-brown)' : 'none',
                  borderTopLeftRadius: t && l ? 4 : 0,
                  borderTopRightRadius: t && !l ? 4 : 0,
                  borderBottomLeftRadius: !t && l ? 4 : 0,
                  borderBottomRightRadius: !t && !l ? 4 : 0,
                }}/>
              );
            })}
          </div>
        </div>

        <div style={{ padding: '0 32px 20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ width: 44, height: 44, borderRadius: 12, border: '1px solid rgba(250,249,245,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <I.gallery size={20}/>
            </div>
            <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'var(--bg-cream)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <div style={{ width: 58, height: 58, borderRadius: '50%', border: '2px solid var(--accent-brown)' }}/>
            </div>
            <div style={{ width: 44, height: 44, borderRadius: 12, border: '1px solid rgba(250,249,245,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <I.flip size={20}/>
            </div>
          </div>
          <div style={{ textAlign: 'center', fontSize: 11, opacity: 0.7, marginTop: 14, fontStyle: 'italic', fontFamily: 'var(--serif)' }}>
            Works best with a single leaf, good light.
          </div>
        </div>
      </div>
    </Phone>
  </Screen>
);

const C2_Analyzing = () => (
  <Screen id="C · 02" name="Scan · analyzing">
    <Phone tab="scan">
      <TopBar left={<I.close size={22}/>} center={null} right={null}/>
      <div className="body" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 24 }}>
        <div style={{ width: '100%', aspectRatio: '1', background: 'linear-gradient(135deg, #3a4432 0%, #566148 100%)', borderRadius: 20, position: 'relative', overflow: 'hidden' }}>
          <svg width="100%" height="100%" viewBox="0 0 100 100">
            <path d="M20 80c20-10 40-40 40-70-30 10-40 40-40 70z" fill="#788c5d" opacity="0.7"/>
          </svg>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div className="h-l" style={{ fontSize: 22 }}>Looking closely…</div>
          <div className="italic muted" style={{ fontSize: 13, marginTop: 6 }}>This usually takes a moment.</div>
        </div>
        <div style={{ position: 'relative', width: 100, height: 100 }}>
          <div className="orbit">
            <svg className="o-leaf" viewBox="0 0 16 16"><path d="M2 14c4 0 8-2 10-6s2-6 2-6-5 1-8 4-4 8-4 8z" fill="#788c5d"/></svg>
            <svg className="o-leaf" viewBox="0 0 16 16"><path d="M2 14c4 0 8-2 10-6s2-6 2-6-5 1-8 4-4 8-4 8z" fill="#d97757"/></svg>
            <svg className="o-leaf" viewBox="0 0 16 16"><path d="M2 14c4 0 8-2 10-6s2-6 2-6-5 1-8 4-4 8-4 8z" fill="#b0aea5"/></svg>
          </div>
        </div>
      </div>
      <div style={{ textAlign: 'center', padding: '0 0 22px' }}>
        <span className="btn-text" style={{ fontSize: 13 }}>Cancel</span>
      </div>
    </Phone>
  </Screen>
);

const C3_Result = () => (
  <Screen id="C · 03" name="Scan · result">
    <Phone tab="scan">
      <TopBar left={<I.close size={22}/>} center={null} right={<I.share size={20}/>}/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div style={{ width: '100%', aspectRatio: '1', background: 'linear-gradient(135deg, #3a4432 0%, #566148 100%)', borderRadius: 18, overflow: 'hidden', position: 'relative' }}>
          <svg width="100%" height="100%" viewBox="0 0 100 100">
            <path d="M20 80c20-10 40-40 40-70-30 10-40 40-40 70z" fill="#a5b98a" opacity="0.8"/>
          </svg>
        </div>
        <div style={{ marginTop: 18 }}>
          <div className="h-l">Fiddle Leaf Fig</div>
          <div className="italic muted" style={{ fontSize: 14 }}>Ficus lyrata</div>
          <div className="chip green" style={{ marginTop: 10 }}>
            <span className="dot green"/> Confident match
          </div>
        </div>
        <div style={{ marginTop: 20, display: 'flex', flexDirection: 'column', gap: 12 }}>
          {[
            [<I.drop size={18}/>, 'Weekly', 'Keep soil moist, not wet.'],
            [<I.sun size={18}/>, 'Bright indirect', 'No direct afternoon sun.'],
            [<I.thermo size={18}/>, '18–24°C', 'Avoid cold windows.'],
          ].map(([ic, t, s], i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '12px 0', borderTop: i ? '1px solid var(--border)' : 'none' }}>
              <div style={{ color: 'var(--accent-green)' }}>{ic}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontFamily: 'var(--serif)', fontSize: 15, fontWeight: 500 }}>{t}</div>
                <div className="muted" style={{ fontSize: 12.5 }}>{s}</div>
              </div>
            </div>
          ))}
        </div>
        <div style={{ marginTop: 20 }}>
          <div className="btn btn-primary">Add to a device</div>
          <div style={{ textAlign: 'center', marginTop: 14 }}>
            <span className="btn-text" style={{ fontSize: 13 }}>View in library</span>
          </div>
          <div style={{ textAlign: 'center', marginTop: 8 }}>
            <span className="fine">Not right? Try another photo.</span>
          </div>
        </div>
      </div>
    </Phone>
  </Screen>
);

const C4_NoMatch = () => (
  <Screen id="C · 04" name="Scan · no match">
    <Phone tab="scan">
      <TopBar left={<I.close size={22}/>} center={null} right={null}/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div style={{ width: '100%', aspectRatio: '1', background: '#3a4432', borderRadius: 18, overflow: 'hidden', position: 'relative', filter: 'grayscale(0.4)' }}>
          <svg width="100%" height="100%" viewBox="0 0 100 100">
            <path d="M30 80c10-10 30-40 30-70-20 10-30 40-30 70z" fill="#788c5d" opacity="0.5"/>
          </svg>
        </div>
        <div style={{ marginTop: 24 }}>
          <div className="h-l">I'm not sure<br/>what this is.</div>
          <div className="body-t muted" style={{ marginTop: 10 }}>
            Try another photo with better light, or browse the library to identify by hand.
          </div>
        </div>
        <div style={{ marginTop: 26 }}>
          <div className="btn btn-primary">Try again</div>
          <div style={{ textAlign: 'center', marginTop: 14 }}>
            <span className="btn-text" style={{ fontSize: 13 }}>Browse the library</span>
          </div>
        </div>
      </div>
    </Phone>
  </Screen>
);

Object.assign(window, { C1_Camera, C2_Analyzing, C3_Result, C4_NoMatch });
