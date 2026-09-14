package com.kidsg.data.mock

import com.kidsg.domain.model.Address
import com.kidsg.domain.model.Category
import com.kidsg.domain.model.Coupon
import com.kidsg.domain.model.DeliveryConfig
import com.kidsg.domain.model.IntentModeInfo
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.OopsEmergencyItem
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.Store
import com.kidsg.domain.model.UserProfile

object KidsGMockData {

    val deliveryConfig = DeliveryConfig(
        baseDeliveryFee = 30.0,
        freeDeliveryThreshold = 199.0,
        platformFee = 5.0,
        taxRatePercent = 5.0,
        minOrderValue = 40.0,
        expressEtaMinutesMin = 12,
        expressEtaMinutesMax = 18
    )

    val partnerStore = Store(
        id = "store_vidya_depot",
        name = "Vidya Book & Stationery Depot",
        locality = "HSR Layout, Sector 4",
        distanceKm = 0.8,
        rating = 4.9,
        prepTimeMinutes = 8,
        address = "12, 17th Cross, 5th Main, HSR Layout, Bengaluru",
        isOpen = true
    )

    val defaultUser = UserProfile(
        id = "user_aarav_sharma",
        name = "Aarav Sharma",
        phone = "+91 91484 73131",
        email = "aarav.student@kidsg.app",
        studentName = "Aarav Sharma",
        studentGrade = "Class 7",
        schoolName = "National Public School, Bengaluru",
        isParentMode = false
    )

    val defaultAddresses = listOf(
        Address(
            id = "addr_home",
            label = "Home",
            recipientName = "Aarav Sharma (Parent: Priya)",
            phoneNumber = "+91 91484 73131",
            addressLine1 = "#402, Sunshine Heights, 14th Main",
            addressLine2 = "HSR Layout, Sector 3",
            city = "Bengaluru",
            pincode = "560102",
            deliveryInstructions = "Please ring the bell and leave with security if unavailable.",
            isDefault = true
        ),
        Address(
            id = "addr_school",
            label = "School Gate",
            recipientName = "Aarav Sharma (Class 7B)",
            phoneNumber = "+91 91484 73131",
            addressLine1 = "National Public School Gate 2",
            addressLine2 = "HSR Layout",
            city = "Bengaluru",
            pincode = "560102",
            deliveryInstructions = "Drop at reception counter for Aarav Sharma.",
            isDefault = false
        )
    )

    val coupons = listOf(
        Coupon(
            code = "KIDSG50",
            title = "50% OFF (Up to ₹100)",
            description = "Applicable on all stationery orders above ₹149",
            discountPercent = 50.0,
            maxDiscount = 100.0,
            minOrderValue = 149.0,
            expiryText = "Valid today"
        ),
        Coupon(
            code = "EXAMREADY",
            title = "20% OFF Exam Essentials",
            description = "Special coupon for exam boards, pens & geometry boxes",
            discountPercent = 20.0,
            maxDiscount = 80.0,
            minOrderValue = 99.0,
            expiryText = "Valid for 3 days"
        ),
        Coupon(
            code = "FIRSTDESK",
            title = "Flat ₹40 OFF First Order",
            description = "Welcome gift for new students & parents",
            discountPercent = 25.0,
            maxDiscount = 40.0,
            minOrderValue = 80.0,
            expiryText = "Welcome coupon"
        )
    )

    val intentModes = listOf(
        IntentModeInfo(
            type = IntentModeType.SCHOOL,
            title = "School Mode",
            subtitle = "Everyday essentials",
            badge = "Class 1-12",
            accentColorHex = "#FF7A00",
            description = "Daily notebooks, smooth gel pens, sharpeners and covers.",
            promptQuestion = "Ready for school tomorrow?"
        ),
        IntentModeInfo(
            type = IntentModeType.EXAM,
            title = "Exam Mode",
            subtitle = "Exam hall ready",
            badge = "Board Approved",
            accentColorHex = "#FFD166",
            description = "Clear pouches, 0.7mm dark black pens, clipboards and geometry kits.",
            promptQuestion = "Got your exam kit sorted?"
        ),
        IntentModeInfo(
            type = IntentModeType.CREATE,
            title = "Create Mode",
            subtitle = "Art & Craft playground",
            badge = "Unleash Ideas",
            accentColorHex = "#C084FC",
            description = "Vibrant oil pastels, acrylics, craft sheets and glue.",
            promptQuestion = "What masterpiece are you making?"
        ),
        IntentModeInfo(
            type = IntentModeType.NEW_TERM,
            title = "New Term",
            subtitle = "Back to school bundles",
            badge = "Complete Kit",
            accentColorHex = "#A7F3D0",
            description = "Full syllabus sets, notebook bundles and book label stickers.",
            promptQuestion = "New term, fresh start!"
        ),
        IntentModeInfo(
            type = IntentModeType.OOPS,
            title = "Oops Mode",
            subtitle = "Forgot something urgently?",
            badge = "12 Min Express",
            accentColorHex = "#FF9ACD",
            description = "Forgot something for tomorrow morning? KidsG it right now!",
            promptQuestion = "Oops! What did you forget?"
        )
    )

    val oopsItems = listOf(
        OopsEmergencyItem("oops_pen", "Forgot my blue pen", "Exam-safe 0.7mm smooth ball/gel", "pen", "✏️", 12),
        OopsEmergencyItem("oops_notebook", "Need a single line notebook", "Classmate 172 pages soft cover", "notebook", "📓", 12),
        OopsEmergencyItem("oops_geometry", "Geometry box tomorrow!", "Maped / Camlin exam compliant set", "geometry", "📐", 14),
        OopsEmergencyItem("oops_project", "Science project chart paper", "A3 chart sheets + glue + markers", "craft", "🎨", 15),
        OopsEmergencyItem("oops_eraser", "Lost eraser & sharpener", "Nataraj dust-free combo pack", "eraser", "🧼", 10)
    )

    val categories = listOf(
        Category("notebooks", "Notebooks", "notebooks", "Single line, spiral, ruled, grid & hardbound", "notebooks", "#FF7A00", 42, true),
        Category("pens_pencils", "Pens & Pencils", "pens-pencils", "Smooth gel pens, ball pens & dark graphite pencils", "pens_pencils", "#FFD166", 58, true),
        Category("art_craft", "Art & Craft", "art-craft", "Oil pastels, sketch pens, water colors & glue sticks", "art_craft", "#C084FC", 35, true),
        Category("school_bags", "School Bags", "school-bags", "Ergonomic student backpacks & protective pencil cases", "bags_accessories", "#FF9ACD", 28, true),
        Category("geometry", "Geometry", "geometry", "Precision compass, math instruments & scales", "geometry", "#7DD3FC", 19, true),
        Category("water_bottles", "Water Bottles", "water-bottles", "Insulated stainless steel & BPA-free flasks", "bottles_lunch", "#93C5FD", 24, true),
        Category("lunch_boxes", "Lunch Boxes", "lunch-boxes", "Thermal insulated bento containers & snack boxes", "bottles_lunch", "#FB923C", 20, true),
        Category("organizers", "Organizers", "organizers", "Sticky notes, highlighters, binder clips & desk stands", "organizers", "#FDE047", 26, true),
        Category("exam_essentials", "Exam Essentials", "exam-essentials", "Clipboards, clear pouches & board-approved pens", "exam_essentials", "#A7F3D0", 22, true)
    )

    val products = listOf(
        // --- NOTEBOOKS ---
        Product(
            id = "prod_classmate_single_line",
            name = "Classmate Notebook",
            brand = "Classmate",
            price = 40.0,
            mrp = 50.0,
            rating = 4.8,
            reviewCount = 1200,
            categoryId = "notebooks",
            description = "Single Line, 200 pages. Premium ozone-treated chlorine-free paper. Smooth paper prevents ink bleed-through.",
            imageUrl = "",
            specifications = mapOf("Pages" to "200 Pages", "Ruling" to "Single Line", "Paper" to "70 GSM"),
            variants = listOf("Single Line", "Four Line", "Square Grid"),
            isAvailable = true,
            stockQuantity = 80,
            isBestSeller = true,
            tags = listOf("Notebook", "Classmate", "Single Line"),
            intentModes = listOf("SCHOOL", "NEW_TERM", "OOPS")
        ),
        Product(
            id = "prod_spiral_notebook_a5",
            name = "Spiral Notebook A5",
            brand = "Classmate Pulse",
            price = 120.0,
            mrp = 140.0,
            rating = 4.7,
            reviewCount = 450,
            categoryId = "notebooks",
            description = "A5, 200 pages twin-wire spiral bound. Micro-perforated pages with polypropylene water-resistant cover.",
            imageUrl = "",
            specifications = mapOf("Size" to "A5", "Pages" to "200 Pages", "Binding" to "Twin Wire Spiral"),
            variants = listOf("Ruled", "Unruled", "Grid"),
            isAvailable = true,
            stockQuantity = 45,
            isBestSeller = true,
            tags = listOf("Spiral", "Classmate", "College"),
            intentModes = listOf("SCHOOL", "NEW_TERM")
        ),
        Product(
            id = "prod_hardbound_notebook",
            name = "Hardbound Notebook",
            brand = "Paperkraft",
            price = 180.0,
            mrp = 220.0,
            rating = 4.9,
            reviewCount = 310,
            categoryId = "notebooks",
            description = "Premium Quality hard case bound notebook with archival-grade paper and satin bookmark ribbon.",
            imageUrl = "",
            specifications = mapOf("Cover" to "Hard Case Bound", "Pages" to "240 Pages", "GSM" to "80 GSM"),
            isAvailable = true,
            stockQuantity = 30,
            isBestSeller = false,
            tags = listOf("Hardbound", "Diary", "Journal"),
            intentModes = listOf("SCHOOL")
        ),
        Product(
            id = "prod_eco_notebook",
            name = "Eco Notebook",
            brand = "Navneet Youva",
            price = 90.0,
            mrp = 105.0,
            rating = 4.6,
            reviewCount = 180,
            categoryId = "notebooks",
            description = "Recycled Paper eco-friendly long book. Unbleached smooth natural shade paper gentle on eyes.",
            imageUrl = "",
            specifications = mapOf("Paper" to "100% Recycled Natural", "Pages" to "172 Pages"),
            isAvailable = true,
            stockQuantity = 50,
            isBestSeller = false,
            tags = listOf("Eco", "Recycled", "Green"),
            intentModes = listOf("SCHOOL")
        ),
        Product(
            id = "prod_maths_grid_book",
            name = "Youva Mathematics Grid Notebook",
            brand = "Navneet",
            price = 55.0,
            mrp = 65.0,
            rating = 4.7,
            reviewCount = 220,
            categoryId = "notebooks",
            description = "Accurate square grid long book engineered for geometry, arithmetic and graphing.",
            imageUrl = "",
            specifications = mapOf("Ruling" to "Square Grid", "Pages" to "172 Pages"),
            isAvailable = true,
            stockQuantity = 60,
            isBestSeller = false,
            tags = listOf("Maths", "Grid", "Square"),
            intentModes = listOf("SCHOOL", "EXAM")
        ),

        // --- PENS & PENCILS ---
        Product(
            id = "prod_camlin_gel_pen_pack",
            name = "Camlin Gel Pen (Pack of 5)",
            brand = "Camlin",
            price = 100.0,
            mrp = 120.0,
            rating = 4.9,
            reviewCount = 890,
            categoryId = "pens_pencils",
            description = "Waterproof Japanese ink formula for effortless flow. Quick-drying smear-proof writing.",
            imageUrl = "",
            specifications = mapOf("Tip" to "0.5 mm", "Ink" to "Waterproof Japanese Gel", "Count" to "5 Pens"),
            isAvailable = true,
            stockQuantity = 75,
            isBestSeller = true,
            tags = listOf("Gel Pen", "Camlin", "Exam"),
            intentModes = listOf("SCHOOL", "EXAM", "OOPS")
        ),
        Product(
            id = "prod_cello_butterflow_pack",
            name = "Cello Butterflow Blue Ball Pens (Pack of 5)",
            brand = "Cello",
            price = 50.0,
            mrp = 60.0,
            rating = 4.8,
            reviewCount = 1500,
            categoryId = "pens_pencils",
            description = "Lubriflow ink system for ultra-smooth skip-free writing. Soft rubber grip prevents hand fatigue.",
            imageUrl = "",
            specifications = mapOf("Tip" to "0.7 mm Swiss Carbide", "Count" to "5 Pens"),
            isAvailable = true,
            stockQuantity = 120,
            isBestSeller = true,
            tags = listOf("Ball Pen", "Cello", "Blue"),
            intentModes = listOf("SCHOOL", "EXAM", "OOPS")
        ),
        Product(
            id = "prod_apsara_platinum_pack",
            name = "Apsara Platinum Extra Dark Pencils (Pack of 10)",
            brand = "Apsara",
            price = 50.0,
            mrp = 60.0,
            rating = 4.8,
            reviewCount = 920,
            categoryId = "pens_pencils",
            description = "Extra dark lead requires less writing pressure. Free eraser and sharpener included inside.",
            imageUrl = "",
            specifications = mapOf("Grade" to "Extra Dark 2B", "Quantity" to "10 Pencils + Eraser + Sharpener"),
            isAvailable = true,
            stockQuantity = 95,
            isBestSeller = true,
            tags = listOf("Pencil", "Apsara", "Dark"),
            intentModes = listOf("SCHOOL", "OOPS")
        ),
        Product(
            id = "prod_doms_neon_mechanical",
            name = "Doms Neon Mechanical Pencil Set 0.7mm",
            brand = "Doms",
            price = 60.0,
            mrp = 75.0,
            rating = 4.7,
            reviewCount = 310,
            categoryId = "pens_pencils",
            description = "Retractable brass mechanism with cushioned lead support to minimize breakage. Free lead tube included.",
            imageUrl = "",
            specifications = mapOf("Lead Size" to "0.7 mm HB", "Body" to "Neon Pastel Barrel"),
            isAvailable = true,
            stockQuantity = 40,
            isBestSeller = false,
            tags = listOf("Mechanical Pencil", "Clutch", "Doms"),
            intentModes = listOf("SCHOOL")
        ),

        // --- ART & CRAFT ---
        Product(
            id = "prod_doms_colour_pencils",
            name = "Doms Colour Pencils (24 Shades)",
            brand = "Doms",
            price = 150.0,
            mrp = 180.0,
            rating = 4.8,
            reviewCount = 670,
            categoryId = "art_craft",
            description = "Bright and vibrant soft core colored pencils for smooth laydown and effortless blending. Includes sharpener.",
            imageUrl = "",
            specifications = mapOf("Shades" to "24 Vibrant Colors", "Shape" to "Hexagonal"),
            isAvailable = true,
            stockQuantity = 55,
            isBestSeller = true,
            tags = listOf("Color Pencils", "Doms", "Drawing"),
            intentModes = listOf("CREATE", "NEW_TERM")
        ),
        Product(
            id = "prod_doms_oil_pastels",
            name = "Doms Extra Long Oil Pastels (25 Shades)",
            brand = "Doms",
            price = 140.0,
            mrp = 160.0,
            rating = 4.9,
            reviewCount = 540,
            categoryId = "art_craft",
            description = "Rich pigment with smooth inter-mixability. Non-toxic and child-safe. Includes scraping tool.",
            imageUrl = "",
            specifications = mapOf("Shades" to "25 Shades", "Length" to "75mm Extra Long"),
            isAvailable = true,
            stockQuantity = 40,
            isBestSeller = true,
            tags = listOf("Oil Pastels", "Doms", "Art"),
            intentModes = listOf("CREATE")
        ),
        Product(
            id = "prod_fevistick_pack",
            name = "Fevistick Super Glue Stick 15g (Pack of 2)",
            brand = "Pidilite",
            price = 65.0,
            mrp = 75.0,
            rating = 4.8,
            reviewCount = 780,
            categoryId = "art_craft",
            description = "Mess-free rotary glue stick for paper, photos, chart projects, and crafts. Fast drying and non-staining.",
            imageUrl = "",
            specifications = mapOf("Weight" to "15g x 2", "Type" to "Clear Glue Stick"),
            isAvailable = true,
            stockQuantity = 90,
            isBestSeller = true,
            tags = listOf("Glue", "Fevistick", "Craft"),
            intentModes = listOf("SCHOOL", "CREATE", "OOPS")
        ),
        Product(
            id = "prod_faber_sketch_pens",
            name = "Faber-Castell Connector Sketch Pens (15 Shades)",
            brand = "Faber-Castell",
            price = 120.0,
            mrp = 140.0,
            rating = 4.8,
            reviewCount = 390,
            categoryId = "art_craft",
            description = "Click-together connector caps encourage building and creative play while coloring. Washable ink.",
            imageUrl = "",
            specifications = mapOf("Shades" to "15 Shades", "Ink" to "Food Grade Washable"),
            isAvailable = true,
            stockQuantity = 35,
            isBestSeller = false,
            tags = listOf("Sketch Pens", "Faber", "Art"),
            intentModes = listOf("CREATE")
        ),

        // --- SCHOOL BAGS & POUCHES ---
        Product(
            id = "prod_skybags_school_backpack",
            name = "Skybags Junior School Backpack",
            brand = "Skybags",
            price = 899.0,
            mrp = 1299.0,
            rating = 4.9,
            reviewCount = 420,
            categoryId = "school_bags",
            description = "Ergonomic 3-compartment water-resistant backpack with padded air-mesh back support and bottle pockets.",
            imageUrl = "",
            specifications = mapOf("Capacity" to "28 Liters", "Material" to "Water-Repellent Polyester", "Warranty" to "1 Year"),
            variants = listOf("Navy Blue", "Teal Turquoise", "Coral Pink"),
            isAvailable = true,
            stockQuantity = 25,
            isBestSeller = true,
            tags = listOf("Bag", "Backpack", "Skybags"),
            intentModes = listOf("NEW_TERM", "SCHOOL")
        ),
        Product(
            id = "prod_hardtop_pencil_case",
            name = "3D Embossed Hardtop Pencil Box",
            brand = "KidsG Studio",
            price = 299.0,
            mrp = 399.0,
            rating = 4.8,
            reviewCount = 280,
            categoryId = "school_bags",
            description = "Shock-proof EVA hardtop case with dual mesh pockets and pencil organizer flaps. Protects delicate stationery.",
            imageUrl = "",
            specifications = mapOf("Material" to "Shockproof EVA", "Closure" to "Smooth Double Zipper"),
            variants = listOf("Space Astronaut", "Dino Explorer", "Cute Unicorn"),
            isAvailable = true,
            stockQuantity = 35,
            isBestSeller = true,
            tags = listOf("Pencil Box", "Hardtop", "Pouch"),
            intentModes = listOf("SCHOOL", "NEW_TERM")
        ),
        Product(
            id = "prod_transparent_exam_pouch",
            name = "Clear Transparent Zip Exam Pouch",
            brand = "KidsG Basics",
            price = 60.0,
            mrp = 80.0,
            rating = 4.7,
            reviewCount = 510,
            categoryId = "school_bags",
            description = "Board exam approved 100% see-through waterproof PVC stationery pouch with sturdy zip puller.",
            imageUrl = "",
            specifications = mapOf("Material" to "Heavy-Gauge Clear PVC", "Dimensions" to "20cm x 12cm"),
            isAvailable = true,
            stockQuantity = 80,
            isBestSeller = false,
            tags = listOf("Pouch", "Exam", "Clear"),
            intentModes = listOf("EXAM", "OOPS")
        ),

        // --- GEOMETRY ---
        Product(
            id = "prod_maped_geometry_box",
            name = "Maped Technic Mathematical Geometry Box",
            brand = "Maped",
            price = 160.0,
            mrp = 185.0,
            rating = 4.8,
            reviewCount = 540,
            categoryId = "geometry",
            description = "Precision die-cast compass with auto-lock screw. Clear transparent set squares, protractor, and 15cm ruler.",
            imageUrl = "",
            specifications = mapOf("Case" to "Metallic Tin", "Contents" to "Compass, Divider, 2 Set Squares, Protractor, Scale"),
            isAvailable = true,
            stockQuantity = 45,
            isBestSeller = true,
            tags = listOf("Geometry", "Maped", "Maths"),
            intentModes = listOf("SCHOOL", "EXAM", "OOPS")
        ),
        Product(
            id = "prod_camlin_exam_geometry",
            name = "Camlin Scholar Mathematical Drawing Set",
            brand = "Camlin Kokuyo",
            price = 130.0,
            mrp = 150.0,
            rating = 4.7,
            reviewCount = 380,
            categoryId = "geometry",
            description = "Specially designed self-centering compass and divider with non-rusting instruments for CBSE and ICSE exams.",
            imageUrl = "",
            specifications = mapOf("Material" to "Anti-Rust Nickel Plated Alloy", "Includes" to "Sturdy Metal Case"),
            isAvailable = true,
            stockQuantity = 40,
            isBestSeller = false,
            tags = listOf("Geometry", "Camlin", "Scholar"),
            intentModes = listOf("SCHOOL", "EXAM")
        ),
        Product(
            id = "prod_nataraj_steel_scale",
            name = "Nataraj 30cm Stainless Steel Precision Scale",
            brand = "Nataraj",
            price = 45.0,
            mrp = 55.0,
            rating = 4.8,
            reviewCount = 610,
            categoryId = "geometry",
            description = "Laser engraved centimeter and inch graduations with rounded safety corners and anti-slip backing.",
            imageUrl = "",
            specifications = mapOf("Length" to "30 cm / 12 inch", "Material" to "Tempered Stainless Steel"),
            isAvailable = true,
            stockQuantity = 70,
            isBestSeller = false,
            tags = listOf("Scale", "Ruler", "Steel"),
            intentModes = listOf("SCHOOL", "OOPS")
        ),

        // --- WATER BOTTLES ---
        Product(
            id = "prod_milton_thermosteel_bottle",
            name = "Milton Thermosteel Kids Water Bottle 750ml",
            brand = "Milton",
            price = 499.0,
            mrp = 649.0,
            rating = 4.9,
            reviewCount = 360,
            categoryId = "water_bottles",
            description = "Double-wall vacuum insulated SS 304 flask keeps water cold for 24 hours. Leak-proof flip spout with strap.",
            imageUrl = "",
            specifications = mapOf("Capacity" to "750 ml", "Material" to "SS 304 Food Grade", "Insulation" to "24 Hr Cold / 18 Hr Hot"),
            variants = listOf("Ocean Blue", "Pastel Pink", "Mint Green"),
            isAvailable = true,
            stockQuantity = 30,
            isBestSeller = true,
            tags = listOf("Bottle", "Milton", "Steel"),
            intentModes = listOf("SCHOOL", "NEW_TERM")
        ),
        Product(
            id = "prod_cello_duro_bottle",
            name = "Cello Duro Stainless Steel School Bottle 600ml",
            brand = "Cello",
            price = 299.0,
            mrp = 399.0,
            rating = 4.7,
            reviewCount = 290,
            categoryId = "water_bottles",
            description = "Single-wall lightweight stainless steel bottle with ergonomic silicone grip sleeve and carabiner hook.",
            imageUrl = "",
            specifications = mapOf("Capacity" to "600 ml", "Material" to "100% Rust-Proof Steel"),
            isAvailable = true,
            stockQuantity = 40,
            isBestSeller = false,
            tags = listOf("Bottle", "Cello", "Water"),
            intentModes = listOf("SCHOOL")
        ),

        // --- LUNCH BOXES ---
        Product(
            id = "prod_borosil_insulated_lunch",
            name = "Borosil Insulated Stainless Steel Lunch Box",
            brand = "Borosil",
            price = 549.0,
            mrp = 699.0,
            rating = 4.8,
            reviewCount = 330,
            categoryId = "lunch_boxes",
            description = "3-tier leakproof stainless steel containers with insulated thermal carry bag. Keeps roti and curry warm till recess.",
            imageUrl = "",
            specifications = mapOf("Containers" to "3 Tiffin Boxes + Spoon", "Bag" to "Thermal Insulated Zip Pouch"),
            isAvailable = true,
            stockQuantity = 25,
            isBestSeller = true,
            tags = listOf("Lunch Box", "Tiffin", "Borosil"),
            intentModes = listOf("SCHOOL", "NEW_TERM")
        ),
        Product(
            id = "prod_kids_bento_snack_box",
            name = "Kids 4-Compartment Bento Lunch Box",
            brand = "KidsG Studio",
            price = 349.0,
            mrp = 449.0,
            rating = 4.7,
            reviewCount = 210,
            categoryId = "lunch_boxes",
            description = "BPA-free microwave safe bento box with separate fruit and dip compartments. Airtight silicone lock seal.",
            imageUrl = "",
            specifications = mapOf("Compartments" to "4 Sections", "Material" to "BPA-Free Food Grade Polypropylene"),
            variants = listOf("Sky Blue", "Sunny Yellow", "Cherry Blossom"),
            isAvailable = true,
            stockQuantity = 35,
            isBestSeller = false,
            tags = listOf("Bento", "Snack Box", "Lunch"),
            intentModes = listOf("SCHOOL")
        ),

        // --- ORGANIZERS ---
        Product(
            id = "prod_faber_highlighters",
            name = "Faber-Castell Textliner Highlighters (Pack of 5)",
            brand = "Faber-Castell",
            price = 110.0,
            mrp = 125.0,
            rating = 4.9,
            reviewCount = 440,
            categoryId = "organizers",
            description = "Super fluorescent water-based ink. Chisel tip marks in 3 line widths (5mm, 2mm, 1mm). Non-smudging.",
            imageUrl = "",
            specifications = mapOf("Colors" to "Yellow, Green, Pink, Orange, Blue", "Tip" to "Chisel 1-5 mm"),
            isAvailable = true,
            stockQuantity = 50,
            isBestSeller = true,
            tags = listOf("Highlighter", "Faber", "Revision"),
            intentModes = listOf("SCHOOL", "EXAM")
        ),
        Product(
            id = "prod_post_it_sticky_notes",
            name = "3M Post-It Pastel Sticky Notes Pad (Pack of 4)",
            brand = "3M",
            price = 140.0,
            mrp = 160.0,
            rating = 4.8,
            reviewCount = 370,
            categoryId = "organizers",
            description = "Removable self-stick notes with strong adhesive that leaves no residue on textbook pages or desk.",
            imageUrl = "",
            specifications = mapOf("Sheets" to "100 Sheets per Pad (400 Total)", "Size" to "3 in x 3 in"),
            isAvailable = true,
            stockQuantity = 45,
            isBestSeller = true,
            tags = listOf("Sticky Notes", "Post-It", "Study"),
            intentModes = listOf("SCHOOL", "EXAM")
        ),
        Product(
            id = "prod_desk_pen_stand_organizer",
            name = "Rotating Desk Stationery Stand & Organizer",
            brand = "KidsG Studio",
            price = 199.0,
            mrp = 249.0,
            rating = 4.7,
            reviewCount = 180,
            categoryId = "organizers",
            description = "360-degree rotating 5-slot organizer for scissors, rulers, pens, markers and erasers. Keep study desk tidy.",
            imageUrl = "",
            specifications = mapOf("Rotation" to "360 Degree Ball Bearing", "Compartments" to "5 Deep Sections"),
            isAvailable = true,
            stockQuantity = 30,
            isBestSeller = false,
            tags = listOf("Organizer", "Pen Stand", "Desk"),
            intentModes = listOf("SCHOOL", "NEW_TERM")
        ),

        // --- EXAM ESSENTIALS ---
        Product(
            id = "prod_camlin_exam_clipboard",
            name = "Camlin Exam Grade Hardboard Clipboard",
            brand = "Camlin",
            price = 90.0,
            mrp = 110.0,
            rating = 4.7,
            reviewCount = 320,
            categoryId = "exam_essentials",
            description = "Smooth calibrated writing board with heavy-duty nickel plated clip. Approved for ICSE, CBSE, and State boards.",
            imageUrl = "",
            specifications = mapOf("Material" to "Hardened MDF Wood", "Clip" to "Heavy-Duty Spring Clip"),
            isAvailable = true,
            stockQuantity = 50,
            isBestSeller = true,
            tags = listOf("Clipboard", "Exam", "Camlin"),
            intentModes = listOf("EXAM", "OOPS")
        )
    )

}
