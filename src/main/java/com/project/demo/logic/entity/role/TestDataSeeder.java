package com.project.demo.logic.entity.role;

import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.logic.entity.transaction.TblTransaction;
import com.project.demo.logic.entity.transaction.TblTransactionRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Order(4)
public class TestDataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final TblPublicationRepository publicationRepository;
    private final TblTransactionRepository transactionRepository;
    private final TblDirectionRepository directionRepository;
    private final UserRepository userRepository;

    public TestDataSeeder(TblPublicationRepository publicationRepository,
                          TblTransactionRepository transactionRepository,
                          TblDirectionRepository directionRepository,
                          UserRepository userRepository) {
        this.publicationRepository = publicationRepository;
        this.transactionRepository = transactionRepository;
        this.directionRepository = directionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        // Solo crear datos si la BD está vacía
        if (publicationRepository.count() > 0) {
            return;
        }
        createTestData();
    }

    private void createTestData() {
        // Buscar usuarios SELLER para asignar publicaciones
        Optional<TblUser> seller1 = userRepository.findByEmail("carlos.test@gmail.com");
        Optional<TblUser> seller2 = userRepository.findByEmail("sofia.test@gmail.com");
        Optional<TblUser> seller3 = userRepository.findByEmail("diego.test@gmail.com");
        Optional<TblUser> seller4 = userRepository.findByEmail("laura.test@gmail.com");
        Optional<TblUser> seller5 = userRepository.findByEmail("miguel.test@gmail.com");

        // Buscar usuarios BUYER para transacciones
        Optional<TblUser> buyer1 = userRepository.findByEmail("maria.test@gmail.com");
        Optional<TblUser> buyer2 = userRepository.findByEmail("ana.test@gmail.com");

        if (seller1.isEmpty() || seller2.isEmpty() || buyer1.isEmpty()) {
            return; // TestUserSeeder no ha corrido aún
        }

        // Crear dirección de prueba
        TblDirection direction = new TblDirection();
        direction.setProvince("San José");
        direction.setProvinceId("1");
        direction.setCanton("Central");
        direction.setCantonId("01");
        direction.setDistrict("Carmen");
        direction.setDistrictId("01");
        direction.setOtherDetails("Barrio Escalante, 100m norte del parque");
        direction = directionRepository.save(direction);

        TblDirection direction2 = new TblDirection();
        direction2.setProvince("Alajuela");
        direction2.setProvinceId("2");
        direction2.setCanton("San Ramón");
        direction2.setCantonId("02");
        direction2.setDistrict("Santiago");
        direction2.setDistrictId("01");
        direction2.setOtherDetails("Frente a la iglesia");
        direction2 = directionRepository.save(direction2);

        TblDirection direction3 = new TblDirection();
        direction3.setProvince("Heredia");
        direction3.setProvinceId("4");
        direction3.setCanton("Barva");
        direction3.setCantonId("02");
        direction3.setDistrict("San Pablo");
        direction3.setDistrictId("03");
        direction3.setOtherDetails("200m sur del estadio");
        direction3 = directionRepository.save(direction3);

        TblUser[] sellers = {
                seller1.get(), seller2.get(), seller3.get(),
                seller4.orElse(seller1.get()), seller5.orElse(seller2.get())
        };
        TblDirection[] directions = {direction, direction2, direction3};

        // =============================================
        // VENTAS (25 publicaciones tipo "Venta")
        // =============================================
        String[][] salesData = {
                {"Venta de Ternero Holstein",    "Bovino",  "Holstein",           "Macho",  "150", "350000"},
                {"Vaca lechera Jersey",          "Bovino",  "Jersey",             "Hembra", "450", "800000"},
                {"Novillo Brahman engorde",      "Bovino",  "Brahman",            "Macho",  "380", "620000"},
                {"Cerda reproductora Landrace",  "Porcino", "Landrace",           "Hembra", "120", "280000"},
                {"Caballo criollo domado",       "Equino",  "Criollo",            "Macho",  "420", "1500000"},
                {"Oveja Pelibuey joven",         "Ovino",   "Pelibuey",           "Hembra", "35",  "95000"},
                {"Toro Angus reproductor",       "Bovino",  "Angus",              "Macho",  "600", "2200000"},
                {"Gallinas ponedoras lote x20",  "Avícola", "Rhode Island Red",   "Hembra", "3",   "60000"},
                {"Cabra Saanen lechera",         "Caprino", "Saanen",             "Hembra", "55",  "180000"},
                {"Potranca Pura Sangre",         "Equino",  "Pura Sangre",        "Hembra", "350", "3500000"},
                {"Ternera Simmental",            "Bovino",  "Simmental",          "Hembra", "130", "420000"},
                {"Cerdo York duroc engorde",     "Porcino", "Duroc",              "Macho",  "95",  "150000"},
                {"Vaca Charolais preñada",       "Bovino",  "Charolais",          "Hembra", "520", "1100000"},
                {"Cordero Dorper joven",         "Ovino",   "Dorper",             "Macho",  "28",  "85000"},
                {"Yegua Quarter Horse",          "Equino",  "Quarter Horse",      "Hembra", "400", "2800000"},
                {"Ternero Hereford",             "Bovino",  "Hereford",           "Macho",  "140", "380000"},
                {"Conejo Rex lote x5",           "Cunícola","Rex",                "Macho",  "4",   "35000"},
                {"Vaca Gyr lechera",             "Bovino",  "Gyr",               "Hembra", "430", "750000"},
                {"Cerda Hampshire gestante",     "Porcino", "Hampshire",          "Hembra", "110", "320000"},
                {"Novilla Nelore",               "Bovino",  "Nelore",             "Hembra", "300", "550000"},
                {"Búfalo de agua joven",         "Bovino",  "Búfalo de agua",     "Macho",  "250", "900000"},
                {"Pato Muscovy lote x10",        "Avícola", "Muscovy",            "Macho",  "5",   "45000"},
                {"Cabra Boer engorde",           "Caprino", "Boer",               "Macho",  "60",  "200000"},
                {"Vaca Brown Swiss",             "Bovino",  "Brown Swiss",        "Hembra", "470", "850000"},
                {"Lechón Pietrain destetado",    "Porcino", "Pietrain",           "Macho",  "15",  "55000"},
        };

        for (int i = 0; i < salesData.length; i++) {
            String[] d = salesData[i];
            TblPublication pub = new TblPublication();
            pub.setTitle(d[0]);
            pub.setSpecie(d[1]);
            pub.setRace(d[2]);
            pub.setGender(d[3]);
            pub.setWeight(Long.parseLong(d[4]));
            pub.setBirthDate(LocalDate.of(2023, (i % 12) + 1, (i % 28) + 1));
            pub.setSenasaCertificate("SENASA-V-" + String.format("%04d", i + 1));
            pub.setPrice(Long.parseLong(d[5]));
            pub.setType("Venta");
            pub.setState("Activa");
            pub.setCreationDate(Instant.now());
            pub.setDirection(directions[i % directions.length]);
            pub.setUser(sellers[i % sellers.length]);
            publicationRepository.save(pub);
        }

        // =============================================
        // SUBASTAS (25 publicaciones tipo "Subasta")
        // =============================================
        String[][] auctionsData = {
                {"Subasta Toro Brahman élite",      "Bovino",  "Brahman",        "Macho",  "700", "1000000", "50000"},
                {"Subasta Yegua árabe pura",        "Equino",  "Árabe",          "Hembra", "380", "2000000", "100000"},
                {"Subasta Vaca Holstein campeona",   "Bovino",  "Holstein",       "Hembra", "500", "1500000", "75000"},
                {"Subasta Semental Angus",           "Bovino",  "Angus",          "Macho",  "650", "3000000", "150000"},
                {"Subasta Cerdo Ibérico",            "Porcino", "Ibérico",        "Macho",  "130", "500000",  "25000"},
                {"Subasta Potrillo Lusitano",        "Equino",  "Lusitano",       "Macho",  "280", "4000000", "200000"},
                {"Subasta Vaca Jersey registrada",   "Bovino",  "Jersey",         "Hembra", "420", "1200000", "60000"},
                {"Subasta Lote ovejas Texel x10",    "Ovino",   "Texel",          "Hembra", "40",  "800000",  "40000"},
                {"Subasta Caballo Paso Fino",        "Equino",  "Paso Fino",      "Macho",  "400", "5000000", "250000"},
                {"Subasta Toro Simmental genética",  "Bovino",  "Simmental",      "Macho",  "680", "2500000", "125000"},
                {"Subasta Cabra Alpine lechera",     "Caprino", "Alpine",         "Hembra", "50",  "250000",  "15000"},
                {"Subasta Vaca Charolais élite",     "Bovino",  "Charolais",      "Hembra", "550", "1800000", "90000"},
                {"Subasta Potro Criollo",            "Equino",  "Criollo",        "Macho",  "320", "1500000", "75000"},
                {"Subasta Cerda Berkshire",          "Porcino", "Berkshire",       "Hembra", "100", "400000",  "20000"},
                {"Subasta Toro Hereford campeón",    "Bovino",  "Hereford",       "Macho",  "720", "3500000", "175000"},
                {"Subasta Vaca Nelore importada",    "Bovino",  "Nelore",         "Hembra", "480", "2000000", "100000"},
                {"Subasta Carnero Suffolk",          "Ovino",   "Suffolk",         "Macho",  "65",  "350000",  "20000"},
                {"Subasta Yegua Appaloosa",          "Equino",  "Appaloosa",      "Hembra", "370", "3000000", "150000"},
                {"Subasta Toro Guzerat",             "Bovino",  "Guzerat",        "Macho",  "630", "1800000", "90000"},
                {"Subasta Lote cabras Toggenburg",   "Caprino", "Toggenburg",     "Hembra", "45",  "600000",  "30000"},
                {"Subasta Búfalo Murrah",            "Bovino",  "Murrah",         "Macho",  "500", "1200000", "60000"},
                {"Subasta Cerdo Large White",        "Porcino", "Large White",    "Macho",  "115", "350000",  "18000"},
                {"Subasta Vaca Pardo Suizo",         "Bovino",  "Pardo Suizo",    "Hembra", "460", "1600000", "80000"},
                {"Subasta Potro Frisón",             "Equino",  "Frisón",         "Macho",  "450", "6000000", "300000"},
                {"Subasta Toro Limousin",            "Bovino",  "Limousin",       "Macho",  "670", "2800000", "140000"},
        };

        for (int i = 0; i < auctionsData.length; i++) {
            String[] d = auctionsData[i];
            TblPublication pub = new TblPublication();
            pub.setTitle(d[0]);
            pub.setSpecie(d[1]);
            pub.setRace(d[2]);
            pub.setGender(d[3]);
            pub.setWeight(Long.parseLong(d[4]));
            pub.setBirthDate(LocalDate.of(2022, (i % 12) + 1, (i % 28) + 1));
            pub.setSenasaCertificate("SENASA-S-" + String.format("%04d", i + 1));
            pub.setPrice(Long.parseLong(d[5]));
            pub.setMinimumIncrease(Integer.parseInt(d[6]));
            pub.setStartDate(LocalDateTime.now().minusDays(i));
            pub.setEndDate(LocalDateTime.now().plusDays(30 - i));
            pub.setType("Subasta");
            pub.setState("Activa");
            pub.setCreationDate(Instant.now());
            pub.setDirection(directions[i % directions.length]);
            pub.setUser(sellers[i % sellers.length]);
            publicationRepository.save(pub);
        }

        // =============================================
        // TRANSACCIONES / FACTURAS (10)
        // =============================================
        TblUser[] buyers = {buyer1.get(), buyer2.get()};

        // Obtener algunas publicaciones de venta para asociar a transacciones
        List<TblPublication> allSales = publicationRepository.findAll().stream()
                .filter(p -> "Venta".equals(p.getType()))
                .limit(10)
                .toList();

        for (int i = 0; i < Math.min(10, allSales.size()); i++) {
            TblPublication salePub = allSales.get(i);

            TblTransaction tx = new TblTransaction();
            tx.setStatus("APPROVED");
            tx.setUser(buyers[i % buyers.length]);

            BigDecimal subTotal = BigDecimal.valueOf(salePub.getPrice());
            BigDecimal taxAmount = subTotal.multiply(BigDecimal.valueOf(0.13));
            tx.setSubTotal(subTotal);
            tx.setTax(taxAmount);
            tx.setTotal(subTotal.add(taxAmount));
            tx.setCreationDate(LocalDateTime.now().minusDays(i));

            tx = transactionRepository.save(tx);

            // Asociar publicación a la transacción
            salePub.setTransaction(tx);
            salePub.setState("Vendida");
            publicationRepository.save(salePub);
        }
    }
}
