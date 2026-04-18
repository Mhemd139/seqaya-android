// B1 Home empty, B2 Home populated, B3 Device detail, B4–B6 Add-device wizard

const DeviceCard = ({ name, species, moisture, dot, lastWater, lastSeen, seed = 3, waterAt = [14, 30] }) => (
  <div className="card">
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
      <div className="h-m">{name}</div>
      <span className={"dot " + dot}/>
    </div>
    <div className="italic muted" style={{ fontSize: 13, marginTop: 2 }}>{species}</div>
    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', margin: '18px 0 4px' }}>
      <div className="num-card">{moisture}<span style={{ fontSize: 22, color: 'var(--text-tertiary)', marginLeft: 4 }}>%</span></div>
      <div className="caps" style={{ fontSize: 9 }}>Last 24h</div>
    </div>
    <div style={{ height: 50 }}><Sparkline seed={seed} waterAt={waterAt} w={300} h={50}/></div>
    <div style={{ height: 1, background: 'var(--border)', margin: '12px 0 10px' }}/>
    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-secondary)' }}>
      <span>Last watered {lastWater}</span>
      <span>Seen {lastSeen}</span>
    </div>
  </div>
);

const B1_HomeEmpty = () => (
  <Screen id="B · 01" name="Home, empty">
    <Phone tab="home">
      <div className="topbar">
        <span className="wordmark">Seqaya</span>
        <div className="avatar">M</div>
      </div>
      <div style={{ flex: 1, padding: '0 28px', display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 22, position: 'relative' }}>
        <div>
          <div className="caps" style={{ marginBottom: 10 }}>A quiet beginning</div>
          <div className="h-xl" style={{ fontSize: 36 }}>Your first plant<br/>is waiting.</div>
          <div className="body-t muted" style={{ marginTop: 12, maxWidth: '30ch' }}>
            Add your Seqaya device to start. It takes about a minute.
          </div>
        </div>
        <div>
          <div className="btn btn-primary" style={{ maxWidth: 260 }}>Add a device</div>
          <div style={{ marginTop: 14 }}><span className="btn-text" style={{ fontSize: 13 }}>Browse the plant library</span></div>
        </div>
        <div style={{ position: 'absolute', right: -10, bottom: -10, opacity: 0.9, transform: 'rotate(8deg)' }}>
          <PlantSketch kind="monstera" size={160}/>
        </div>
      </div>
    </Phone>
  </Screen>
);

const B2_HomePop = ({ dark = false }) => (
  <Screen id={dark ? "B · 02 ◗" : "B · 02"} name={dark ? "Home, populated · dark" : "Home, populated"}>
    <Phone tab="home" dark={dark}>
      <div className="topbar">
        <span className="wordmark">Seqaya</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div className="icon-btn"><I.plus size={20}/></div>
          <div className="avatar">M</div>
        </div>
      </div>
      <div style={{ padding: '2px 20px 0' }}>
        <div style={{ background: 'var(--accent-brown-soft)', border: '1px solid rgba(217,119,87,0.25)', borderRadius: 14, padding: '10px 14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <span className="dot brown"/>
            <div style={{ fontSize: 12.5, color: '#a85535' }}><b>Basil</b> is thirsty.</div>
          </div>
          <span style={{ fontSize: 12, color: '#a85535', fontWeight: 600 }}>Review</span>
        </div>
      </div>
      <div className="body" style={{ display: 'flex', flexDirection: 'column', gap: 14, overflowY: 'auto' }}>
        <DeviceCard name="Figgy" species="Ficus lyrata" moisture="62" dot="green" lastWater="3h ago" lastSeen="2 min" seed={1}/>
        <DeviceCard name="Basil" species="Ocimum basilicum" moisture="34" dot="brown" lastWater="1d ago" lastSeen="just now" seed={4} waterAt={[]}/>
        <DeviceCard name="The Monster" species="Monstera deliciosa" moisture="58" dot="green" lastWater="7h ago" lastSeen="5 min" seed={7}/>
        <div style={{ height: 10 }}/>
      </div>
    </Phone>
  </Screen>
);

const B3_DeviceDetail = ({ dark = false }) => {
  const pts = [];
  for (let i = 0; i < 40; i++) {
    const v = 55 + Math.sin(i * 0.35) * 14 + Math.cos(i * 0.18) * 6;
    pts.push([i / 39 * 300, 80 - ((v - 30) / 50) * 80]);
  }
  const d = pts.map((p, i) => (i === 0 ? 'M' : 'L') + p[0].toFixed(1) + ',' + p[1].toFixed(1)).join(' ');
  const waterIdx = [6, 14, 22, 30, 36];
  return (
    <Screen id={dark ? "B · 03 ◗" : "B · 03"} name={dark ? "Device · dark" : "Device detail"}>
      <Phone tab="home" dark={dark}>
        <TopBar
          left={<I.back size={22}/>}
          center={<span style={{ fontFamily: 'var(--serif)', fontSize: 16, fontWeight: 500 }}>Figgy</span>}
          right={<I.more size={20}/>}
        />
        <div className="body" style={{ overflowY: 'auto' }}>
          <div className="italic muted" style={{ fontSize: 13 }}>Ficus lyrata</div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, marginTop: 4 }}>
            <span className="num-hero">62</span>
            <span style={{ fontSize: 22, color: 'var(--text-tertiary)', fontFamily: 'var(--serif)' }}>%</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 6 }}>
            <div className="chip green"><span className="dot green"/> Thriving</div>
            <span className="fine">Target 60%</span>
          </div>

          <div style={{ marginTop: 22, display: 'flex', gap: 4, background: 'var(--bg-cream-lightest)', padding: 4, borderRadius: 100, border: '1px solid var(--border)' }}>
            {['24h', '7d', '30d'].map((t, i) => (
              <div key={t} style={{ flex: 1, textAlign: 'center', padding: '7px 0', borderRadius: 100, fontSize: 12, fontWeight: 500, background: i === 1 ? 'var(--bg-cream)' : 'transparent', color: i === 1 ? 'var(--text-primary)' : 'var(--text-secondary)', border: i === 1 ? '1px solid var(--border)' : 'none' }}>{t}</div>
            ))}
          </div>

          <div style={{ height: 90, margin: '14px 0 6px', position: 'relative' }}>
            <svg viewBox="0 0 300 80" preserveAspectRatio="none" style={{ width: '100%', height: 80 }}>
              <line x1="0" y1="30" x2="300" y2="30" stroke="var(--border)" strokeDasharray="2 3"/>
              <path d={d} fill="none" stroke="var(--accent-green)" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
              {waterIdx.map(i => {
                const p = pts[i]; return p && <circle key={i} cx={p[0]} cy={p[1]} r="2.8" fill="var(--accent-brown)"/>;
              })}
            </svg>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontFamily: 'var(--mono)', fontSize: 9, letterSpacing: '0.12em', color: 'var(--text-tertiary)', textTransform: 'uppercase' }}>
            <span>Mon</span><span>Wed</span><span>Fri</span><span>Sun</span>
          </div>

          <div style={{ marginTop: 26 }}>
            <div className="h-m" style={{ fontSize: 18 }}>Care for a Fiddle Leaf</div>
            <ul style={{ margin: '10px 0 0', padding: 0, listStyle: 'none', display: 'flex', flexDirection: 'column', gap: 10 }}>
              {[
                'Water when the top inch of soil feels dry.',
                'Bright indirect light, no direct afternoon sun.',
                'Avoid drafts and cold windows.',
                'Wipe the leaves with a damp cloth monthly.',
              ].map((t, i) => (
                <li key={i} style={{ display: 'flex', gap: 10, fontSize: 13.5, lineHeight: 1.5 }}>
                  <span style={{ width: 5, height: 5, borderRadius: '50%', background: 'var(--accent-green)', marginTop: 8, flexShrink: 0 }}/>
                  <span>{t}</span>
                </li>
              ))}
            </ul>
          </div>

          <div style={{ marginTop: 24 }}>
            <div className="caps" style={{ marginBottom: 6 }}>Device</div>
            {[
              ['Serial', 'SQ-F3A-2971'],
              ['Plant', 'Ficus lyrata'],
              ['Nickname', 'Figgy'],
              ['Last seen', '2 min ago'],
              ['Firmware', 'v1.4.2'],
            ].map(([k, v], i) => (
              <div key={k} style={{ display: 'flex', justifyContent: 'space-between', padding: '13px 0', borderTop: i ? '1px solid var(--border)' : 'none', fontSize: 13.5 }}>
                <span className="muted">{k}</span>
                <span>{v}</span>
              </div>
            ))}
            <div style={{ padding: '14px 0', borderTop: '1px solid var(--border)', color: 'var(--accent-brown)', fontSize: 13.5, fontWeight: 500 }}>Delete device</div>
          </div>
        </div>
      </Phone>
    </Screen>
  );
};

const B4_PickPlant = () => (
  <Screen id="B · 04" name="Add device · Pick a plant">
    <Phone noTabs>
      <StepHeader step="1"/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div className="h-l" style={{ marginTop: 6 }}>What are you growing?</div>
        <div style={{ marginTop: 18, display: 'flex', alignItems: 'center', gap: 10, borderBottom: '1px solid var(--border-strong)', paddingBottom: 10 }}>
          <I.leaf size={18} style={{ color: 'var(--text-secondary)' }}/>
          <span style={{ fontSize: 14, color: 'var(--text-tertiary)' }}>Search plants</span>
        </div>
        <div style={{ marginTop: 18, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          {[
            ['fig', 'Fiddle Leaf', 'Ficus lyrata', true],
            ['monstera', 'Monstera', 'M. deliciosa'],
            ['pothos', 'Pothos', 'Epipremnum'],
            ['snake', 'Snake plant', 'Sansevieria'],
            ['basil', 'Basil', 'Ocimum basilicum'],
            ['succulent', 'Succulent', 'Echeveria'],
          ].map(([k, n, s, sel]) => (
            <div key={k} style={{ border: sel ? '1.5px solid var(--accent-brown)' : '1px solid var(--border)', borderRadius: 16, padding: 12, background: sel ? 'var(--accent-brown-soft)' : 'var(--bg-cream-lightest)' }}>
              <div style={{ display: 'flex', justifyContent: 'center' }}><PlantSketch kind={k} size={72}/></div>
              <div style={{ fontFamily: 'var(--serif)', fontSize: 14.5, fontWeight: 500, marginTop: 2 }}>{n}</div>
              <div className="italic muted" style={{ fontSize: 11.5 }}>{s}</div>
            </div>
          ))}
        </div>
        <div style={{ textAlign: 'center', margin: '18px 0 10px' }}>
          <span className="btn-text" style={{ fontSize: 13 }}>Can't find it? Add a custom plant.</span>
        </div>
      </div>
      <div style={{ padding: '0 20px 18px' }}>
        <div className="btn btn-primary">Next</div>
      </div>
    </Phone>
  </Screen>
);

const B5_WiFi = () => (
  <Screen id="B · 05" name="Add device · Wi-Fi">
    <Phone noTabs>
      <StepHeader step="2"/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div className="h-l" style={{ marginTop: 6 }}>Which Wi-Fi?</div>
        <div className="body-t muted" style={{ marginTop: 10 }}>
          Your device connects to this network. 2.4&nbsp;GHz only — most home networks are fine.
        </div>

        <div style={{ marginTop: 26 }}>
          <span className="field-label">Network</span>
          <div className="field">
            <span style={{ flex: 1 }}>Windowsill_5C</span>
            <span className="caps" style={{ fontSize: 9 }}>Change</span>
          </div>
        </div>

        <div style={{ marginTop: 18 }}>
          <span className="field-label">Password</span>
          <div className="field">
            <span style={{ flex: 1, letterSpacing: 4 }}>••••••••••</span>
            <I.eye size={18} style={{ color: 'var(--text-secondary)' }}/>
          </div>
          <div className="fine" style={{ marginTop: 6 }}>We never store your password.</div>
        </div>

        <div style={{ marginTop: 28, display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0', borderTop: '1px solid var(--border)', borderBottom: '1px solid var(--border)' }}>
          <span style={{ fontSize: 13.5 }}>Choose a different network</span>
          <I.back size={16} style={{ transform: 'rotate(180deg)', color: 'var(--text-secondary)' }}/>
        </div>
      </div>
      <div style={{ padding: '0 20px 18px' }}>
        <div className="btn btn-primary">Next</div>
      </div>
    </Phone>
  </Screen>
);

const B6_TapPhone = () => (
  <Screen id="B · 06" name="Add device · Tap">
    <Phone noTabs>
      <StepHeader step="3" right=""/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div className="h-l" style={{ marginTop: 6 }}>Tap your phone<br/>to the device.</div>
        <div style={{ margin: '38px 0 30px' }}>
          <div className="nfc-wrap">
            <div className="nfc-wave w1"/>
            <div className="nfc-wave w2"/>
            <div className="nfc-wave w3"/>
            <svg width="220" height="160" viewBox="0 0 220 160" style={{ position: 'relative' }}>
              <rect x="18" y="32" width="44" height="96" rx="8" fill="none" stroke="var(--text-primary)" strokeWidth="1.3"/>
              <circle cx="40" cy="120" r="2.5" fill="var(--text-primary)"/>
              <rect x="152" y="60" width="50" height="36" rx="5" fill="none" stroke="var(--text-primary)" strokeWidth="1.3"/>
              <path d="M168 74h18M168 80h12" stroke="var(--text-primary)" strokeWidth="1"/>
            </svg>
          </div>
        </div>
        <div className="body-t" style={{ textAlign: 'center', maxWidth: '30ch', margin: '0 auto', color: 'var(--text-secondary)' }}>
          Hold the back of your phone<br/>against the Seqaya logo on the device.
        </div>
      </div>
      <div style={{ textAlign: 'center', padding: '0 0 22px' }}>
        <span className="btn-text" style={{ fontSize: 13 }}>Cancel setup</span>
      </div>
    </Phone>
  </Screen>
);

Object.assign(window, { B1_HomeEmpty, B2_HomePop, B3_DeviceDetail, B4_PickPlant, B5_WiFi, B6_TapPhone });
