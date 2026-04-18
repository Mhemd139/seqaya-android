// D1 Browse, D2 Plant detail, D3 Search

const tile = (k, n, s) => (
  <div key={k} style={{ border: '1px solid var(--border)', borderRadius: 16, padding: 14, background: 'var(--bg-cream-lightest)' }}>
    <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 4 }}><PlantSketch kind={k} size={74}/></div>
    <div style={{ fontFamily: 'var(--serif)', fontSize: 14.5, fontWeight: 500 }}>{n}</div>
    <div className="italic muted" style={{ fontSize: 11.5 }}>{s}</div>
  </div>
);

const D1_Browse = ({ dark = false }) => (
  <Screen id={dark ? "D · 01 ◗" : "D · 01"} name={dark ? "Library · dark" : "Library"}>
    <Phone tab="library" dark={dark}>
      <div className="topbar">
        <span className="wordmark">Library</span>
        <div className="icon-btn"><I.search size={20}/></div>
      </div>
      <div style={{ padding: '4px 20px 10px', display: 'flex', gap: 8, overflowX: 'auto' }}>
        {[['All', true], ['Herbs'], ['Leafy'], ['Succulents'], ['Flowering'], ['Tropical']].map(([t, a]) => (
          <div key={t} className={"chip " + (a ? "active" : "")} style={{ flexShrink: 0 }}>{t}</div>
        ))}
      </div>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div className="caps" style={{ marginTop: 6 }}>Popular this month</div>
        <div className="h-m" style={{ margin: '2px 0 12px' }}>For the patient gardener</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {tile('fig', 'Fiddle Leaf', 'Ficus lyrata')}
          {tile('monstera', 'Monstera', 'M. deliciosa')}
        </div>

        <div className="caps" style={{ marginTop: 22 }}>Easy to start</div>
        <div className="h-m" style={{ margin: '2px 0 12px' }}>Forgiving friends</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {tile('pothos', 'Pothos', 'Epipremnum')}
          {tile('snake', 'Snake plant', 'Sansevieria')}
        </div>

        <div className="caps" style={{ marginTop: 22 }}>Herbs</div>
        <div className="h-m" style={{ margin: '2px 0 12px' }}>The kitchen shelf</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {tile('basil', 'Basil', 'Ocimum basilicum')}
          {tile('rosemary', 'Rosemary', 'Salvia rosmarinus')}
          {tile('mint', 'Mint', 'Mentha')}
          {tile('succulent', 'Jade', 'Crassula ovata')}
        </div>

        <div style={{ textAlign: 'center', margin: '22px 0 10px' }}>
          <span className="btn-text" style={{ fontSize: 13 }}>Suggest a plant</span>
        </div>
      </div>
    </Phone>
  </Screen>
);

const D2_PlantDetail = () => (
  <Screen id="D · 02" name="Plant detail">
    <Phone tab="library">
      <TopBar left={<I.back size={22}/>} center={null} right={<I.bookmark size={20}/>}/>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div style={{ display: 'flex', justifyContent: 'center', padding: '8px 0 12px' }}>
          <PlantSketch kind="fig" size={150}/>
        </div>
        <div className="h-l">Fiddle Leaf Fig</div>
        <div className="italic muted" style={{ fontSize: 14 }}>Ficus lyrata</div>
        <div className="body-t" style={{ marginTop: 10, color: 'var(--text-secondary)' }}>
          A slow-growing West African native with glossy, violin-shaped leaves. Famously theatrical — rewards attention, punishes neglect.
        </div>

        <div style={{ marginTop: 20, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {[
            [<I.drop size={18}/>, 'Water', 'Weekly, keep moist'],
            [<I.sun size={18}/>, 'Light', 'Bright indirect'],
            [<I.thermo size={18}/>, 'Temp', '18–24°C'],
            [<I.wind size={18}/>, 'Humidity', 'Moderate'],
          ].map(([ic, t, s], i) => (
            <div key={i} style={{ border: '1px solid var(--border)', borderRadius: 14, padding: 14, background: 'var(--bg-cream-lightest)' }}>
              <div style={{ color: 'var(--accent-green)' }}>{ic}</div>
              <div className="caps" style={{ marginTop: 8 }}>{t}</div>
              <div style={{ fontFamily: 'var(--serif)', fontSize: 14, marginTop: 2 }}>{s}</div>
            </div>
          ))}
        </div>

        <div style={{ marginTop: 22 }}>
          <div className="h-m" style={{ fontSize: 18 }}>Care notes</div>
          <div className="body-t" style={{ marginTop: 8, color: 'var(--text-primary)' }}>
            Rotate the pot a quarter-turn each week for even growth. Avoid moving it frequently — fiddle leaves sulk when relocated. Let the top inch of soil dry out between waterings.
          </div>
        </div>

        <div style={{ marginTop: 22 }}>
          <div className="h-m" style={{ fontSize: 18 }}>Troubles</div>
          <div style={{ marginTop: 10, display: 'flex', flexDirection: 'column', gap: 10 }}>
            {[
              ['Yellow leaves', 'Usually overwatering. Let the soil dry.'],
              ['Drooping', 'Check for drafts or underwatering.'],
              ['Brown edges', 'Air is too dry or water has too much chlorine.'],
            ].map(([t, s], i) => (
              <div key={i} style={{ display: 'flex', gap: 12, padding: '10px 0', borderTop: i ? '1px solid var(--border)' : 'none' }}>
                <div style={{ color: 'var(--accent-brown)', flexShrink: 0 }}><I.alert size={18}/></div>
                <div>
                  <div style={{ fontFamily: 'var(--serif)', fontSize: 14, fontWeight: 500 }}>{t}</div>
                  <div className="muted" style={{ fontSize: 12.5, marginTop: 1 }}>{s}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div style={{ marginTop: 22 }}>
          <div className="btn btn-primary">Assign to a device</div>
        </div>
      </div>
    </Phone>
  </Screen>
);

const D3_Search = () => (
  <Screen id="D · 03" name="Library · search">
    <Phone tab="library">
      <div className="topbar">
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 10, borderBottom: '1.5px solid var(--text-primary)', paddingBottom: 6 }}>
          <I.search size={18}/>
          <span style={{ fontSize: 15 }}>fig<span style={{ display: 'inline-block', width: 1.5, height: 14, background: 'var(--accent-brown)', marginLeft: 2, verticalAlign: 'middle' }}/></span>
        </div>
        <div className="icon-btn" style={{ marginLeft: 8 }}><I.close size={20}/></div>
      </div>
      <div className="body" style={{ overflowY: 'auto' }}>
        <div className="caps" style={{ marginTop: 4 }}>Recent</div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 8 }}>
          {['monstera', 'basil', 'low light', 'orchid'].map(t => (
            <div key={t} className="chip" style={{ background: 'transparent', border: '1px solid var(--border)' }}>{t}</div>
          ))}
        </div>

        <div className="caps" style={{ marginTop: 22 }}>Results</div>
        <div style={{ marginTop: 8 }}>
          {[
            ['fig', 'Fiddle Leaf Fig', 'Ficus lyrata'],
            ['fig', 'Weeping Fig', 'Ficus benjamina'],
            ['fig', 'Rubber Fig', 'Ficus elastica'],
            ['fig', 'Creeping Fig', 'Ficus pumila'],
          ].map(([k, n, s], i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '12px 0', borderTop: i ? '1px solid var(--border)' : 'none' }}>
              <div style={{ width: 48, height: 48, background: 'var(--bg-cream-lightest)', borderRadius: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                <PlantSketch kind={k} size={42}/>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontFamily: 'var(--serif)', fontSize: 15, fontWeight: 500 }}>{n}</div>
                <div className="italic muted" style={{ fontSize: 12.5 }}>{s}</div>
              </div>
              <I.back size={16} style={{ transform: 'rotate(180deg)', color: 'var(--text-tertiary)' }}/>
            </div>
          ))}
        </div>
      </div>
    </Phone>
  </Screen>
);

Object.assign(window, { D1_Browse, D2_PlantDetail, D3_Search });
