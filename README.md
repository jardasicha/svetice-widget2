# Světice → Praha — minimální widget

Maximálně zjednodušená verze widgetu pro Android. Žádná dynamika, žádné odkazy na drawable, žádné API. Jen statický 2×2 widget se třemi ukázkovými časy.

Cílem je nejdřív ověřit, že **widget jde vůbec přidat na plochu** (na Samsung One UI byly problémy s pokročilejšími verzemi). Až tato verze projde, můžeme postupně přidávat funkce.

## Co je v widgetu

- Černé pozadí
- Bílý nadpis "Světice → Praha"
- 3 řádky se statickými časy (`06:12`, `07:42 +4 07:46`, `08:54 +12 09:06`)

Žádný update, žádný klik, žádná logika. Maximálně holé minimum.

## Jak ho dostat do telefonu (GitHub Actions)

### 1. Založ nový GitHub repository
- Vpravo nahoře "+" → **New repository**
- Pojmenuj jak chceš (např. `svetice-min`)
- Nech Public, **nezatrhuj** žádnou inicializaci
- Create

### 2. Nahraj soubory
- Rozbal stažený ZIP
- Otevři rozbalenou složku
- **Cmd+Shift+.** (Finder na Mac) ať vidíš skryté soubory
- Cmd+A → označit vše → přetáhnout do GitHub okna
- **Commit changes**

⚠️ Pokud má někdo Mac a `.github` se nedaří nahrát, vytvoř ji přímo v editoru GitHubu:
- Add file → Create new file
- Název: `.github/workflows/build.yml`
- Obsah zkopíruj z lokálního `.github/workflows/build.yml`

### 3. Build proběhne automaticky
- Záložka **Actions** → měl bys vidět běžící workflow
- Po ~3 minutách zelená fajfka
- Klikni na běh → dolů **Artifacts** → stáhni `SveticeWidget-debug` (ZIP s APK uvnitř)

### 4. Instalace v Androidu
- Rozbal ZIP, dostaneš `app-debug.apk`
- Pošli ho do telefonu
- Klepni → povol instalaci z neznámých zdrojů → instaluj
- Otevři appku — uvidíš info s návodem
- Dlouhý stisk na ploše → Widgety → najdi **Světice → Praha** → přetáhni na plochu

## Co očekávat

**Pokud widget jde přidat:** Skvěle, framework funguje. Budeme postupně přidávat funkce (dynamické časy, klik, auto-update).

**Pokud widget nejde přidat ani teď:** Problém je systémový (Samsung One UI ↔ Android 16 ↔ launcher). V tom případě půjdeme jinou cestou (např. KWGT widget z Play Store).
