# 🤖 Robot Süpürge Simülasyonu

[![build](https://github.com/NGlory7/Robot-Vacuum-Cleaning-Simulation/actions/workflows/build.yml/badge.svg)](https://github.com/NGlory7/Robot-Vacuum-Cleaning-Simulation/actions/workflows/build.yml)

Bir robot süpürgenin 16 × 16 m'lik (256 m²) bir odayı farklı rota algoritmalarıyla nasıl temizlediğini gösteren
JavaFX masaüstü simülasyonu. Odaya eşya ve kir yerleştirip algoritmaları kıyaslayabilirsiniz.

*BZ 214 Görsel Programlama dersi kapsamında geliştirildi.*

## Özellikler

- **4 temizlik algoritması**
  - **Akıllı Rota** (varsayılan): temizlenmemiş en yakın alana gider; engellerin etrafından BFS ile yol bulur.
  - **Spiral**: bulunduğu yerden dışa doğru genişleyen kare spiral çizer; spiral tıkanırsa Akıllı Rota'ya devreder.
  - **Duvar Takip**: engele çarpana kadar düz gider, çarpınca döner; duvar ve eşya kenarlarını izler.
  - **Rastgele**: her adımda gidilebilir yönlerden birini rastgele seçer (karşılaştırma için).
- **Oda düzenleme:** fareyle bitki (1×1), tekli koltuk (2×2), üçlü koltuk (4×2), yemek masası (3×3),
  TV ünitesi (1×4) ve halı (4×3) ekleyin; sağ tıkla silin.
- **Kir türleri:** toz, sıvı ve leke; her biri bataryayı farklı tüketir. Halı üzerinde güç tüketimi artar.
- **Batarya ve hazneler:** batarya, toz haznesi ve sıvı haznesi göstergeleri; batarya azalınca ya da
  "İstasyona Dön" ile robot şarj istasyonuna geri döner.
- **Canlı istatistikler:** temizlenen / kalan alan (m²), süre, toplanan toz; hız ayarı (1–5×).

## Çalıştırma

Java 21 ve Maven gerekir (proje Maven wrapper'ı ile gelir, ayrıca Maven kurmanız gerekmez).

```bash
./mvnw clean javafx:run        # Windows: mvnw.cmd clean javafx:run
```

## Yapı

```
src/main/java/com/example/robotvacuumsimulator/
├── Launcher.java                 giriş noktası
├── controller/
│   ├── GameController.java       simülasyon döngüsü, batarya ve istasyon mantığı
│   ├── PathFinder.java           BFS ile engelden kaçan yol bulma
│   └── algorithms/               Akıllı Rota, Spiral, Duvar Takip, Rastgele
├── model/                        GridMap, Cell, Robot, Furniture, kir ve eşya türleri
└── view/                         SimulationGUI ve eşya çizicileri
src/main/resources/assets/        robot ve eşya görselleri
```

## Teşekkür

Ders hocası ve katkıda bulunan herkese, özellikle **Ahmet Nusret Toprak**'a teşekkürler.

## Lisans

[MIT](LICENSE)
