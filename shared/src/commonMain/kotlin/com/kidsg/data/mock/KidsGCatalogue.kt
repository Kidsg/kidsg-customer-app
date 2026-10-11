package com.kidsg.data.mock

import com.kidsg.domain.model.Category
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.ProductBundle
import com.kidsg.domain.model.ProductQa
import com.kidsg.domain.model.ProductReview
import com.kidsg.domain.model.Subcategory

object KidsGCatalogue {

    val categories: List<Category> = listOf(
        Category(
            id = "cat_stationery",
            name = "Stationery",
            slug = "stationery",
            description = "Notebooks, pens, pencils, erasers & daily desk essentials",
            iconName = "notebooks",
            accentHex = "#FF7A00",
            itemCount = 60,
            isHeroCategory = true
        ),
        Category(
            id = "cat_art_craft",
            name = "Arts & Crafts",
            slug = "art-craft",
            description = "Colours, sketch pens, clay, craft sheets & glue adhesives",
            iconName = "art_craft",
            accentHex = "#C084FC",
            itemCount = 45,
            isHeroCategory = true
        ),
        Category(
            id = "cat_school_kits",
            name = "School Kits",
            slug = "school-kits",
            description = "Grade-wise complete academic packs & board exam combos",
            iconName = "exam_essentials",
            accentHex = "#A7F3D0",
            itemCount = 25,
            isHeroCategory = true
        ),
        Category(
            id = "cat_school_bags",
            name = "School Bags",
            slug = "school-bags",
            description = "Ergonomic student backpacks, trolley bags & pencil cases",
            iconName = "bags_accessories",
            accentHex = "#FF9ACD",
            itemCount = 30,
            isHeroCategory = true
        ),
        Category(
            id = "cat_water_bottles",
            name = "Water Bottles",
            slug = "water-bottles",
            description = "Insulated stainless steel flasks & BPA-free sippers",
            iconName = "water_bottles",
            accentHex = "#7DD3FC",
            itemCount = 25,
            isHeroCategory = false
        ),
        Category(
            id = "cat_lunch_boxes",
            name = "Lunch Boxes",
            slug = "lunch-boxes",
            description = "Thermal insulated bento containers & leak-proof tiffins",
            iconName = "lunch_boxes",
            accentHex = "#FFD166",
            itemCount = 25,
            isHeroCategory = false
        ),
        Category(
            id = "cat_geometry",
            name = "Geometry Instruments",
            slug = "geometry",
            description = "Precision compasses, math boxes, steel scales & set squares",
            iconName = "geometry",
            accentHex = "#38BDF8",
            itemCount = 25,
            isHeroCategory = false
        ),
        Category(
            id = "cat_files_folders",
            name = "Files & Folders",
            slug = "files-folders",
            description = "Ring binders, document portfolios & clear report files",
            iconName = "files_folders",
            accentHex = "#FB923C",
            itemCount = 20,
            isHeroCategory = false
        ),
        Category(
            id = "cat_organizers",
            name = "Organizers & Accessories",
            slug = "organizers",
            description = "Desk pen stands, book ends, sticky flags & page markers",
            iconName = "organizers",
            accentHex = "#F472B6",
            itemCount = 22,
            isHeroCategory = false
        ),
        Category(
            id = "cat_exam_essentials",
            name = "Exam Essentials",
            slug = "exam-essentials",
            description = "Clear exam pouches, board clipboards & approved dark pens",
            iconName = "exam_essentials",
            accentHex = "#34D399",
            itemCount = 25,
            isHeroCategory = false
        )
    )

    val subcategories: List<Subcategory> = listOf(
        // Stationery subcategories
        Subcategory("sub_notebooks", "cat_stationery", "Notebooks", "notebooks", "Single line, spiral, ruled, grid & long books", "notebook", 22),
        Subcategory("sub_long_books", "cat_stationery", "Long Books", "long-books", "200 to 300 page stitched long books", "notebook", 8),
        Subcategory("sub_drawing_books", "cat_stationery", "Drawing Books", "drawing-books", "Cartridge & sketching sheets", "palette", 6),
        Subcategory("sub_registers", "cat_stationery", "Registers", "registers", "Hardbound accounting & practical records", "notebook", 6),
        Subcategory("sub_pens", "cat_stationery", "Pens", "pens", "Smooth gel pens, ball pens & rollerballs", "pen", 22),
        Subcategory("sub_pencils", "cat_stationery", "Pencils", "pencils", "Graphite 2B, HB & mechanical pencils", "pencil", 22),
        Subcategory("sub_erasers", "cat_stationery", "Erasers", "erasers", "Dust-free, kneaded & precision pen erasers", "eraser", 21),
        Subcategory("sub_sharpeners", "cat_stationery", "Sharpeners", "sharpeners", "Dual hole, canister & table sharpeners", "sharpener", 21),
        Subcategory("sub_highlighters", "cat_stationery", "Highlighters & Markers", "highlighters-markers", "Pastel highlighters & whiteboard markers", "highlighter", 12),
        Subcategory("sub_geometry_inst", "cat_stationery", "Geometry Instruments", "geometry-instruments", "Precision compasses & scales", "ruler", 21),
        Subcategory("sub_sticky_notes", "cat_stationery", "Sticky Notes", "sticky-notes", "Neon sticky pads & page indexing flags", "sticky", 10),
        Subcategory("sub_correction", "cat_stationery", "Correction Supplies", "correction-supplies", "White tape pens & correction fluid", "pen", 8),
        Subcategory("sub_paper_products", "cat_stationery", "Paper Products", "paper-products", "Ruled loose sheets, graph paper & charts", "notebook", 10),

        // Arts & Crafts subcategories
        Subcategory("sub_drawing_colouring", "cat_art_craft", "Drawing & Colouring", "drawing-colouring", "Oil pastels, poster colours, brush pens & paints", "palette", 22),
        Subcategory("sub_crayons", "cat_art_craft", "Crayons", "crayons", "Wax crayons & plastic triangular crayons", "pencil", 8),
        Subcategory("sub_colour_pencils", "cat_art_craft", "Colour Pencils", "colour-pencils", "Bi-colour & soft core pencils", "pencil", 8),
        Subcategory("sub_sketch_pens", "cat_art_craft", "Sketch Pens", "sketch-pens", "Waterproof & fine tip sketch markers", "pen", 8),
        Subcategory("sub_watercolours", "cat_art_craft", "Watercolours", "watercolours", "Cake pans & artist watercolour tubes", "palette", 8),
        Subcategory("sub_paint_brushes", "cat_art_craft", "Paint Brushes", "paint-brushes", "Flat, round & mop brush sets", "palette", 6),
        Subcategory("sub_modelling_dough", "cat_art_craft", "Modelling Dough", "modelling-dough", "Non-toxic scented play clay", "palette", 6),
        Subcategory("sub_craft_products", "cat_art_craft", "Craft Products", "craft-products", "Glue sticks, scissors, foam sheets & glitter", "scissors", 21),
        Subcategory("sub_glue_adhesives", "cat_art_craft", "Glue & Adhesives", "glue-adhesives", "Fevicol, glue drops & glue sticks", "glue", 8),
        Subcategory("sub_craft_paper", "cat_art_craft", "Craft Paper & Origami", "craft-paper", "Origami folding paper & quilling strips", "paper", 8),

        // School Kits subcategories
        Subcategory("sub_school_kits", "cat_school_kits", "School Kits", "school-kits-all", "Curated full year & exam readiness sets", "kit", 22),
        Subcategory("sub_exam_kits", "cat_school_kits", "Exam Kits", "exam-kits", "Board approved transparent pouch packs", "clipboard", 8),
        Subcategory("sub_primary_kits", "cat_school_kits", "Primary School Kits", "primary-school-kits", "Class 1-5 stationery bundles", "kit", 6),
        Subcategory("sub_middle_kits", "cat_school_kits", "Middle School Kits", "middle-school-kits", "Class 6-8 comprehensive packs", "kit", 6),

        // School Bags subcategories
        Subcategory("sub_school_bags", "cat_school_bags", "School Bags", "school-bags-all", "Ergonomic multi-compartment student backpacks", "backpack", 21),
        Subcategory("sub_backpacks", "cat_school_bags", "Backpacks", "backpacks", "Padded back water-resistant bags", "backpack", 10),
        Subcategory("sub_pencil_pouches", "cat_school_bags", "Pencil Pouches", "pencil-pouches", "EVA hardtop & double-decker canvas cases", "pouch", 8),

        // Water Bottles subcategories
        Subcategory("sub_water_bottles", "cat_water_bottles", "Water Bottles", "water-bottles-all", "Vacuum insulated steel & Tritan sippers", "bottle", 21),
        Subcategory("sub_insulated_bottles", "cat_water_bottles", "Insulated Bottles", "insulated-bottles", "24-hr cold & warm flasks", "bottle", 8),

        // Lunch Boxes subcategories
        Subcategory("sub_lunch_boxes", "cat_lunch_boxes", "Lunch Boxes", "lunch-boxes-all", "Stainless steel bento & insulated lunch sets", "lunch", 21),
        Subcategory("sub_insulated_lunch", "cat_lunch_boxes", "Insulated Lunch Boxes", "insulated-lunch", "Thermal jackets & multi-tier boxes", "lunch", 8),

        // Geometry subcategories
        Subcategory("sub_geometry_boxes", "cat_geometry", "Geometry Boxes", "geometry-boxes", "Camlin, Maped & Oxford complete sets", "ruler", 21),

        // Files & Folders subcategories
        Subcategory("sub_files_all", "cat_files_folders", "Files & Binders", "files-binders", "Ring binders & report presentation covers", "folder", 12),

        // Organizers subcategories
        Subcategory("sub_organizers_all", "cat_organizers", "Desk Organizers", "desk-organizers", "Mesh pen stands & book racks", "organizer", 12),

        // Exam Essentials subcategories
        Subcategory("sub_exam_boards", "cat_exam_essentials", "Exam Boards & Pouches", "boards-pouches", "Hard clipboards & transparent pouches", "clipboard", 15)
    )

    val products: List<Product> by lazy {
        KidsGCatalogueProducts.notebooks +
                KidsGCatalogueProductsPart2.pens +
                KidsGCatalogueProductsPart2.pencils +
                KidsGCatalogueProductsPart3.erasers +
                KidsGCatalogueProductsPart3.sharpeners +
                KidsGCatalogueProductsPart4.geometry +
                KidsGCatalogueProductsPart5.drawingAndColouring +
                KidsGCatalogueProductsPart5.craftProducts +
                KidsGCatalogueProductsPart6.schoolBags +
                KidsGCatalogueProductsPart6.waterBottles +
                KidsGCatalogueProductsPart7.lunchBoxes +
                KidsGCatalogueProductsPart7.schoolKits
    }

    private val reviewsStore = mutableListOf<ProductReview>().apply {
        addAll(KidsGCatalogueProductsPart7.sampleReviews)
    }

    private val qaStore = mutableListOf<ProductQa>().apply {
        addAll(KidsGCatalogueProductsPart7.sampleQa)
    }

    fun getReviews(productId: String): List<ProductReview> {
        val specific = reviewsStore.filter { it.productId == productId }
        if (specific.isNotEmpty()) return specific
        // Return realistic default reviews for product
        return listOf(
            ProductReview(
                id = "rev_gen_1_$productId",
                productId = productId,
                authorName = "Sunita R.",
                authorRole = "Parent",
                rating = 5.0,
                dateText = "3 days ago",
                comment = "Excellent school quality! Very durable and genuine brand item.",
                isVerifiedPurchase = true,
                helpfulCount = 8
            ),
            ProductReview(
                id = "rev_gen_2_$productId",
                productId = productId,
                authorName = "Kunal D.",
                authorRole = "Student (Class 9)",
                rating = 4.5,
                dateText = "1 week ago",
                comment = "Works as described. Fast 15-minute delivery before school started.",
                isVerifiedPurchase = true,
                helpfulCount = 5
            )
        )
    }

    fun addReview(review: ProductReview) {
        reviewsStore.add(0, review)
    }

    fun getQa(productId: String): List<ProductQa> {
        val specific = qaStore.filter { it.productId == productId }
        if (specific.isNotEmpty()) return specific
        return listOf(
            ProductQa(
                id = "qa_gen_1_$productId",
                productId = productId,
                question = "Is this genuine school-grade product?",
                answer = "Yes, 100% genuine product sourced directly from brand authorized distributor.",
                answeredBy = "KidsG Support",
                answeredDateText = "Verified"
            ),
            ProductQa(
                id = "qa_gen_2_$productId",
                productId = productId,
                question = "How fast will this reach our house?",
                answer = "KidsG delivers in 12-18 minutes from your nearest local partner stationery hub.",
                answeredBy = "KidsG Support",
                answeredDateText = "Verified"
            )
        )
    }

    fun addQuestion(productId: String, question: String) {
        qaStore.add(
            0,
            ProductQa(
                id = "qa_user_${productId}_${qaStore.size}",
                productId = productId,
                question = question,
                answer = "Thank you for asking! Our support desk has verified this item matches your school syllabus requirements.",
                answeredBy = "KidsG Support",
                answeredDateText = "Just now"
            )
        )
    }

    fun getFrequentlyBoughtTogether(productId: String): ProductBundle? {
        val main = products.find { it.id == productId } ?: return null
        val pen = products.find { it.id == "prod_hauser_xo_blue" } ?: products.firstOrNull { it.categoryId == "cat_stationery" && it.id != productId }
        val pencil = products.find { it.id == "prod_apsara_platinum_dark" } ?: products.firstOrNull { it.id != productId && it.id != pen?.id }
        val items = listOfNotNull(main, pen, pencil).distinctBy { it.id }
        if (items.size < 2) return null
        return ProductBundle(
            id = "bnd_${productId}",
            title = "Frequently Bought Together",
            discountPercent = 10,
            items = items
        )
    }

    fun getRelatedProducts(productId: String, type: String): List<Product> {
        val current = products.find { it.id == productId } ?: return products.take(6)
        return when (type) {
            "brand" -> products.filter { it.brand.equals(current.brand, ignoreCase = true) && it.id != productId }.take(6)
            "class" -> products.filter { it.categoryId == current.categoryId && it.id != productId }.take(6)
            else -> products.filter { it.subcategoryId == current.subcategoryId && it.id != productId }.take(6)
                .ifEmpty { products.filter { it.id != productId }.take(6) }
        }
    }

    fun getSimilarProducts(product: Product): List<Product> = getRelatedProducts(product.id, "similar")

    fun getMoreFromBrand(brand: String, excludeId: String? = null): List<Product> =
        products.filter { it.brand.equals(brand, ignoreCase = true) && it.id != excludeId }.take(8)

    fun getPopularForStudents(): List<Product> {
        return listOfNotNull(
            products.find { it.id == "prod_hauser_xo_blue" },
            products.find { it.id == "prod_apsara_non_dust_jumbo" },
            products.find { it.id == "prod_classmate_single_line" },
            products.find { it.id == "prod_doms_25_oil_pastels" },
            products.find { it.id == "prod_camlin_trinity_set" },
            products.find { it.id == "prod_milton_thermosteel_500ml" }
        )
    }
}
