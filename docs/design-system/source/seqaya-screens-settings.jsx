// E1 Settings, E2 Profile, E3 Delete account + F shared components

const E1_Settings = ({ dark = false }) => (
  <Screen id={dark ? "E · 01 ◗" : "E · 01"} name={dark ? "Settings · dark" : "Settings"}>
    <Phone tab="home" dark={dark}>
      <TopBar left={<I.back size={22}/>} center={<span style={{ fontFamily: 'var(--serif)', fontSize: 16, fontWeight: 500 }}>Settings</span>} right={null}/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div className="caps">Account</div>
        <div style={{ marginTop: 8, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 16, padding: '4px 16px' }}>
          {[
            ['Name', 'Maya'],
            ['Email', 'maya@windowsill.co'],
            ['Edit profile', '→'],
          ].map(([k, v], i) => (
            <div key={k} style={{ display: 'flex', justifyContent: 'space-between', padding: '13px 0', borderTop: i ? '1px solid var(--border)' : 'none', fontSize: 14 }}>
              <span>{k}</span><span className="muted" style={{ fontFamily: k === 'Email' ? 'var(--mono)' : 'inherit', fontSize: k === 'Email' ? 12.5 : 14 }}>{v}</span>
            </div>
          ))}
        </div>

        <div className="caps" style={{ marginTop: 20 }}>Preferences</div>
        <div style={{ marginTop: 8, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 16, padding: '4px 16px' }}>
          {[
            ['Theme', 'System'],
            ['Units', '°C'],
            ['Notifications', <div style={{ width: 36, height: 22, borderRadius: 22, background: 'var(--accent-green)', position: 'relative' }}><div style={{ position: 'absolute', right: 2, top: 2, width: 18, height: 18, borderRadius: '50%', background: '#fff' }}/></div>],
          ].map(([k, v], i) => (
            <div key={k} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '11px 0', borderTop: i ? '1px solid var(--border)' : 'none', fontSize: 14 }}>
              <span>{k}</span><span className="muted">{v}</span>
            </div>
          ))}
        </div>

        <div className="caps" style={{ marginTop: 20 }}>About</div>
        <div style={{ marginTop: 8, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 16, padding: '4px 16px' }}>
          {['Version 2.0.1', 'Privacy policy', 'Terms', 'Open source licenses'].map((k, i) => (
            <div key={k} style={{ padding: '13px 0', borderTop: i ? '1px solid var(--border)' : 'none', fontSize: 14 }}>{k}</div>
          ))}
        </div>

        <div className="caps" style={{ marginTop: 20 }}>Danger zone</div>
        <div style={{ marginTop: 8, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 16, padding: '4px 16px' }}>
          <div style={{ padding: '13px 0', fontSize: 14 }}>Sign out</div>
          <div style={{ padding: '13px 0', borderTop: '1px solid var(--border)', fontSize: 14, color: 'var(--accent-brown)' }}>Delete account</div>
        </div>
      </div>
    </Phone>
  </Screen>
);

const E2_Profile = () => (
  <Screen id="E · 02" name="Profile">
    <Phone>
      <TopBar left={<I.back size={22}/>} center={<span style={{ fontFamily: 'var(--serif)', fontSize: 16, fontWeight: 500 }}>Profile</span>} right={null}/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '16px 0 24px' }}>
          <div style={{ width: 92, height: 92, borderRadius: '50%', background: 'var(--accent-green)', color: 'var(--bg-cream)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--serif)', fontSize: 38, fontWeight: 500 }}>M</div>
          <div style={{ marginTop: 10 }}><span className="btn-text" style={{ fontSize: 13 }}>Change photo</span></div>
        </div>
        <span className="field-label">Display name</span>
        <div className="field"><span style={{ flex: 1 }}>Maya</span></div>
        <div style={{ marginTop: 18 }}>
          <span className="field-label">Email</span>
          <div className="field"><span style={{ flex: 1, fontFamily: 'var(--mono)', fontSize: 13, color: 'var(--text-secondary)' }}>maya@windowsill.co</span></div>
          <div className="fine" style={{ marginTop: 4 }}>Managed by Google. Not editable here.</div>
        </div>
      </div>
      <div style={{ padding: '0 20px 18px' }}>
        <div className="btn btn-primary">Save</div>
      </div>
    </Phone>
  </Screen>
);

const E3_DeleteAccount = () => (
  <Screen id="E · 03" name="Delete account">
    <Phone noTabs>
      <TopBar left={<I.close size={22}/>} center={null} right={null}/>
      <div className="body" style={{ overflowY: 'auto', paddingTop: 20 }}>
        <div className="h-xl" style={{ fontSize: 34 }}>Delete your<br/>account?</div>
        <div className="body-t muted" style={{ marginTop: 14, maxWidth: '34ch' }}>
          This removes your profile, your devices' assignments, and all your plant history. It cannot be undone.
        </div>

        <div style={{ marginTop: 28, padding: 16, background: 'var(--accent-brown-soft)', border: '1px solid rgba(217,119,87,0.25)', borderRadius: 14 }}>
          <div className="caps" style={{ color: '#a85535', marginBottom: 6 }}>Type DELETE to confirm</div>
          <div style={{ background: 'var(--bg-cream)', border: '1px solid var(--border)', borderRadius: 10, padding: '12px 14px', fontFamily: 'var(--mono)', fontSize: 14, letterSpacing: '0.14em' }}>
            DELETE<span style={{ display: 'inline-block', width: 1.5, height: 14, background: 'var(--accent-brown)', marginLeft: 2, verticalAlign: 'middle' }}/>
          </div>
        </div>
      </div>
      <div style={{ padding: '0 20px 18px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div className="btn btn-primary">Delete permanently</div>
        <div className="btn btn-secondary">Cancel</div>
      </div>
    </Phone>
  </Screen>
);

// ---- F. SHARED COMPONENTS ----

const F1_Offline = () => (
  <Screen id="F · 01" name="Offline banner">
    <Phone tab="home">
      <div style={{ background: 'var(--accent-brown)', color: 'var(--bg-cream)', padding: '10px 18px', display: 'flex', alignItems: 'center', gap: 10, fontSize: 12.5 }}>
        <I.alert size={16}/>
        <span style={{ flex: 1 }}>You're offline — showing last known state.</span>
      </div>
      <div className="topbar">
        <span className="wordmark">Seqaya</span>
        <div className="avatar">M</div>
      </div>
      <div className="body" style={{ opacity: 0.65 }}>
        <DeviceCard name="Figgy" species="Ficus lyrata" moisture="62" dot="gray" lastWater="3h ago" lastSeen="offline" seed={1}/>
      </div>
    </Phone>
  </Screen>
);

const F2_Toast = () => (
  <Screen id="F · 02" name="Toast">
    <Phone tab="home">
      <div className="topbar">
        <span className="wordmark">Seqaya</span>
        <div className="avatar">M</div>
      </div>
      <div className="body">
        <DeviceCard name="Figgy" species="Ficus lyrata" moisture="62" dot="green" lastWater="3h ago" lastSeen="2 min" seed={1}/>
      </div>
      <div style={{ position: 'absolute', left: '50%', bottom: 86, transform: 'translateX(-50%)', background: 'var(--text-primary)', color: 'var(--bg-cream)', padding: '10px 18px', borderRadius: 100, fontSize: 13, display: 'flex', alignItems: 'center', gap: 8, zIndex: 3, whiteSpace: 'nowrap' }}>
        <I.check size={14}/> Figgy is now watered.
      </div>
    </Phone>
  </Screen>
);

const F3_DeviceSheet = () => (
  <Screen id="F · 03" name="Device selector sheet">
    <Phone tab="library">
      <div style={{ opacity: 0.35, flex: 1, padding: '14px 20px' }}>
        <div className="h-l">Fiddle Leaf Fig</div>
        <div className="italic muted">Ficus lyrata</div>
      </div>
      <div style={{ background: 'var(--bg-cream)', borderTop: '1px solid var(--border-strong)', borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: '16px 20px 22px', position: 'relative', zIndex: 3 }}>
        <div style={{ width: 40, height: 4, borderRadius: 2, background: 'var(--text-tertiary)', margin: '0 auto 14px' }}/>
        <div className="h-m" style={{ fontSize: 19 }}>Assign to which device?</div>
        <div className="italic muted" style={{ fontSize: 12.5, marginBottom: 12 }}>Choose one of your Seqayas.</div>
        {[
          ['Figgy', 'Ficus lyrata', true],
          ['Basil', 'Ocimum basilicum'],
          ['The Monster', 'Monstera deliciosa'],
        ].map(([n, s, sel], i) => (
          <div key={n} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 2px', borderTop: i ? '1px solid var(--border)' : 'none' }}>
            <div style={{ width: 18, height: 18, borderRadius: '50%', border: '1.5px solid ' + (sel ? 'var(--accent-brown)' : 'var(--border-strong)'), display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {sel && <div style={{ width: 9, height: 9, borderRadius: '50%', background: 'var(--accent-brown)' }}/>}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontFamily: 'var(--serif)', fontSize: 15, fontWeight: 500 }}>{n}</div>
              <div className="italic muted" style={{ fontSize: 12 }}>{s}</div>
            </div>
          </div>
        ))}
        <div className="btn btn-primary" style={{ marginTop: 14 }}>Assign</div>
      </div>
    </Phone>
  </Screen>
);

const F4_PlantSheet = () => (
  <Screen id="F · 04" name="Plant selector sheet">
    <Phone>
      <div style={{ opacity: 0.35, flex: 1, padding: '14px 20px' }}>
        <div className="h-m">Figgy</div>
      </div>
      <div style={{ background: 'var(--bg-cream)', borderTop: '1px solid var(--border-strong)', borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: '16px 20px 22px', position: 'relative', zIndex: 3, maxHeight: 460 }}>
        <div style={{ width: 40, height: 4, borderRadius: 2, background: 'var(--text-tertiary)', margin: '0 auto 14px' }}/>
        <div className="h-m" style={{ fontSize: 19 }}>Change plant</div>
        <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 8, borderBottom: '1px solid var(--border-strong)', paddingBottom: 8 }}>
          <I.search size={16}/>
          <span style={{ fontSize: 13.5, color: 'var(--text-tertiary)' }}>Search plants</span>
        </div>
        <div style={{ marginTop: 14, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
          {['fig', 'monstera', 'pothos', 'basil', 'mint', 'succulent'].map(k => (
            <div key={k} style={{ border: '1px solid var(--border)', borderRadius: 12, padding: 6, background: 'var(--bg-cream-lightest)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <PlantSketch kind={k} size={56}/>
            </div>
          ))}
        </div>
      </div>
    </Phone>
  </Screen>
);

const F5_Empty = () => (
  <Screen id="F · 05" name="Empty state component">
    <Phone>
      <TopBar left={<I.back size={22}/>} center={null} right={null}/>
      <div className="body" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', gap: 18, padding: '0 32px' }}>
        <LeafMark size={80} color="#788c5d"/>
        <div>
          <div className="h-l" style={{ fontSize: 26 }}>Nothing yet,<br/>and that's okay.</div>
          <div className="body-t muted" style={{ marginTop: 10 }}>
            When a plant shows up, it will wait here for you.
          </div>
        </div>
        <div className="btn btn-primary" style={{ maxWidth: 220 }}>Add a plant</div>
      </div>
    </Phone>
  </Screen>
);

Object.assign(window, { E1_Settings, E2_Profile, E3_DeleteAccount, F1_Offline, F2_Toast, F3_DeviceSheet, F4_PlantSheet, F5_Empty });
