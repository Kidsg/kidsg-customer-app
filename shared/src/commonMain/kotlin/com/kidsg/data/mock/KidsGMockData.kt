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
        Category("notebooks", "Notebooks", "notebooks", "Single line, ruled, grid, unruled & spirals", "notebooks", "#FF7A00", 42, true),
        Category("pens_pencils", "Pens & Pencils", "pens-pencils", "Gel, ballpoint, fountain & graphite pencils", "pens_pencils", "#FFD166", 58, true),
        Category("art_craft", "Art & Craft", "art-craft", "Paints, pastels, sketchbooks & origami sheets", "art_craft", "#C084FC", 35, true),
        Category("geometry", "Geometry", "geometry", "Mathematical instruments, set squares & compass", "geometry", "#7DD3FC", 19, true),
        Category("exam_essentials", "Exam Essentials", "exam-essentials", "Clipboards, clear pouches & hall ticket pens", "exam_essentials", "#A7F3D0", 24, true),
        Category("bags_accessories", "Bags & Pouches", "bags-accessories", "Pencil boxes, backpacks & desk organizers", "bags_accessories", "#FF9ACD", 30, false),
        Category("bottles_lunch", "Bottles & Lunch", "bottles-lunch", "Insulated stainless steel water bottles & boxes", "bottles_lunch", "#93C5FD", 16, false),
        Category("organizers", "Organizers", "organizers", "Sticky notes, highlighters & binder clips", "organizers", "#FDE047", 22, false)
    )

    val products = listOf(
        Product(
            id = "prod_classmate_single_line",
            name = "Classmate Pulse Single Line Notebook",
            brand = "Classmate",
            price = 60.0,
            mrp = 70.0,
            rating = 4.8,
            reviewCount = 340,
            categoryId = "notebooks",
            description = "Premium ozone-treated elemental chlorine-free paper. Smooth paper prevents ink bleed-through. 172 pages.",
            imageUrl = "",
            specifications = mapOf(
                "Pages" to "172 Pages",
                "Ruling" to "Single Line",
                "Paper Density" to "70 GSM",
                "Binding" to "Soft Bound Pin"
            ),
            variants = listOf("Single Line", "Ruled", "Grid", "Unruled"),
            isAvailable = true,
            stockQuantity = 45,
            isBestSeller = true,
            tags = listOf("Classmate", "Notebook", "School"),
            intentModes = listOf("SCHOOL", "NEW_TERM")
        ),
        Product(
            id = "prod_camlin_gel_set",
            name = "Camlin Kokuyo Gel Pen Set (Pack of 5)",
            brand = "Camlin",
            price = 180.0,
            mrp = 200.0,
            rating = 4.9,
            reviewCount = 215,
            categoryId = "pens_pencils",
            description = "Waterproof Japanese ink formula for effortless flow. Quick-drying smear-proof writing. Ideal for neat homework.",
            imageUrl = "",
            specifications = mapOf(
                "Tip Size" to "0.5 mm",
                "Ink Color" to "Assorted (3 Blue, 1 Black, 1 Red)",
                "Ink Type" to "Waterproof Gel",
                "Refillable" to "Yes"
            ),
            variants = listOf("0.5 mm", "0.7 mm"),
            isAvailable = true,
            stockQuantity = 60,
            isBestSeller = true,
            tags = listOf("Gel Pen", "Camlin", "Exam"),
            intentModes = listOf("SCHOOL", "EXAM", "OOPS")
        ),
        Product(
            id = "prod_apsara_platinum",
            name = "Apsara Platinum Extra Dark Pencils (Pack of 10)",
            brand = "Apsara",
            price = 50.0,
            mrp = 60.0,
            rating = 4.7,
            reviewCount = 480,
            categoryId = "pens_pencils",
            description = "Extra dark lead requires less pressure while writing. Soft wood for easy sharpening. Free eraser and sharpener included.",
            imageUrl = "",
            specifications = mapOf(
                "Grade" to "Extra Dark 2B",
                "Quantity" to "10 Pencils + 1 Eraser + 1 Sharpener",
                "Shape" to "Hexagonal"
            ),
            isAvailable = true,
            stockQuantity = 90,
            isBestSeller = true,
            tags = listOf("Apsara", "Pencil", "Homework"),
            intentModes = listOf("SCHOOL", "OOPS")
        ),
        Product(
            id = "prod_doms_oil_pastels",
            name = "Doms Extra Long Oil Pastels (25 Shades)",
            brand = "Doms",
            price = 140.0,
            mrp = 160.0,
            rating = 4.9,
            reviewCount = 190,
            categoryId = "art_craft",
            description = "Rich pigment with smooth inter-mixability. Non-toxic and safe for children. Includes scraping tool.",
            imageUrl = "",
            specifications = mapOf(
                "Shades" to "25 Colors",
                "Length" to "Extra Long 75mm",
                "Includes" to "1 Scratching Tool"
            ),
            isAvailable = true,
            stockQuantity = 28,
            isBestSeller = true,
            tags = listOf("Doms", "Color", "Drawing", "Art"),
            intentModes = listOf("CREATE", "NEW_TERM")
        ),
        Product(
            id = "prod_maped_geometry_box",
            name = "Maped Technic Mathematical Geometry Box",
            brand = "Maped",
            price = 160.0,
            mrp = 185.0,
            rating = 4.8,
            reviewCount = 110,
            categoryId = "geometry",
            description = "Precision die-cast compass with auto-lock system. Clear transparent set squares, protractor, and 15cm ruler.",
            imageUrl = "",
            specifications = mapOf(
                "Case Material" to "Metallic Tin",
                "Contents" to "Compass, Divider, 2 Set Squares, Protractor, 15cm Scale, Eraser, Sharpener",
                "Accuracy" to "Millimeter Graduated"
            ),
            isAvailable = true,
            stockQuantity = 22,
            isBestSeller = false,
            tags = listOf("Geometry", "Maths", "Exam"),
            intentModes = listOf("SCHOOL", "EXAM", "OOPS")
        ),
        Product(
            id = "prod_classmate_spiral",
            name = "Classmate Spiral Notebook A4 Size",
            brand = "Classmate",
            price = 120.0,
            mrp = 140.0,
            rating = 4.8,
            reviewCount = 85,
            categoryId = "notebooks",
            description = "Twin wire bound spiral notebook with micro-perforated pages. Polypropylene water-resistant front cover.",
            imageUrl = "",
            specifications = mapOf(
                "Size" to "A4",
                "Pages" to "200 Pages",
                "Ruling" to "Ruled with Margin",
                "Cover" to "PP Frost Cover"
            ),
            variants = listOf("Ruled", "Unruled", "Grid"),
            isAvailable = true,
            stockQuantity = 35,
            isBestSeller = true,
            tags = listOf("Classmate", "Spiral", "Notes"),
            intentModes = listOf("SCHOOL", "NEW_TERM")
        ),
        Product(
            id = "prod_camlin_exam_clipboard",
            name = "Camlin Exam Grade Hardboard Clipboard",
            brand = "Camlin",
            price = 90.0,
            mrp = 110.0,
            rating = 4.7,
            reviewCount = 95,
            categoryId = "exam_essentials",
            description = "High-tensile steel clip holds up to 80 sheets firmly without creasing. Smooth surface ideal for board exams.",
            imageUrl = "",
            specifications = mapOf(
                "Material" to "Durable Wood Hardboard",
                "Clip" to "Nickel Plated Heavy Duty Spring Clip",
                "Size" to "Fits A4 and Foolscap Sheets"
            ),
            isAvailable = true,
            stockQuantity = 40,
            isBestSeller = false,
            tags = listOf("Exam", "Pad", "Board"),
            intentModes = listOf("EXAM", "OOPS")
        ),
        Product(
            id = "prod_faber_highlighters",
            name = "Faber-Castell Textliner Highlighters (Pack of 5)",
            brand = "Faber-Castell",
            price = 110.0,
            mrp = 125.0,
            rating = 4.9,
            reviewCount = 140,
            categoryId = "organizers",
            description = "Super fluorescent water-based ink. Chisel tip marks in 3 line widths (5mm, 2mm, 1mm). Does not smudge inkjet print.",
            imageUrl = "",
            specifications = mapOf(
                "Colors" to "Yellow, Green, Pink, Orange, Blue",
                "Tip" to "Chisel 1-5 mm",
                "Ink" to "Water Based Non-Toxic"
            ),
            isAvailable = true,
            stockQuantity = 50,
            isBestSeller = true,
            tags = listOf("Highlighter", "Revision", "Study"),
            intentModes = listOf("SCHOOL", "EXAM")
        ),
        Product(
            id = "prod_fevistick_glue",
            name = "Fevistick Super Glue Stick 15g (Pack of 2)",
            brand = "Pidilite",
            price = 65.0,
            mrp = 75.0,
            rating = 4.8,
            reviewCount = 270,
            categoryId = "art_craft",
            description = "Mess-free rotary glue stick for paper, cardboard, photos, and project work. Dries fast and clear.",
            imageUrl = "",
            specifications = mapOf(
                "Net Weight" to "15g each",
                "Form" to "Solid PVA Stick",
                "Drying" to "Instant Clear"
            ),
            isAvailable = true,
            stockQuantity = 75,
            isBestSeller = false,
            tags = listOf("Glue", "Project", "Craft"),
            intentModes = listOf("SCHOOL", "CREATE", "OOPS")
        ),
        Product(
            id = "prod_doms_water_colors",
            name = "Doms Water Color Cakes (24 Shades)",
            brand = "Doms",
            price = 130.0,
            mrp = 150.0,
            rating = 4.8,
            reviewCount = 160,
            categoryId = "art_craft",
            description = "High opacity water color cakes with excellent spreading and transparency. Free artist hair brush included.",
            imageUrl = "",
            specifications = mapOf(
                "Shades" to "24 Vibrant Colors",
                "Brush Included" to "Series 00 Round Brush",
                "Palette" to "Integrated Lid Palette"
            ),
            isAvailable = true,
            stockQuantity = 30,
            isBestSeller = false,
            tags = listOf("Watercolor", "Art", "Painting"),
            intentModes = listOf("CREATE")
        ),
        Product(
            id = "prod_cello_butterflow",
            name = "Cello Butterflow Classic Blue Ball Pens (Pack of 5)",
            brand = "Cello",
            price = 50.0,
            mrp = 60.0,
            rating = 4.8,
            reviewCount = 390,
            categoryId = "pens_pencils",
            description = "Lubriflow ink system for ultra-smooth skip-free writing. Soft textured rubber grip ensures fatigue-free exam writing.",
            imageUrl = "",
            specifications = mapOf(
                "Tip" to "0.7 mm Swiss Carbide",
                "Ink" to "Blue Low Viscosity",
                "Grip" to "Elastomeric Rubber"
            ),
            isAvailable = true,
            stockQuantity = 110,
            isBestSeller = true,
            tags = listOf("Ball Pen", "Cello", "Exam"),
            intentModes = listOf("SCHOOL", "EXAM", "OOPS")
        ),
        Product(
            id = "prod_youva_grid",
            name = "Navneet Youva Soft Cover Grid Math Notebook",
            brand = "Navneet",
            price = 55.0,
            mrp = 65.0,
            rating = 4.6,
            reviewCount = 70,
            categoryId = "notebooks",
            description = "Square grid ruling engineered for mathematics, arithmetic and science tables. Chlorine-free white paper.",
            imageUrl = "",
            specifications = mapOf(
                "Pages" to "172 Pages",
                "Ruling" to "Square Grid (Maths)",
                "Size" to "Long Book 24cm x 18cm"
            ),
            isAvailable = true,
            stockQuantity = 45,
            isBestSeller = false,
            tags = listOf("Maths", "Grid", "Notebook"),
            intentModes = listOf("SCHOOL")
        )
    )
}
