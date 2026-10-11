package com.kidsg.backend.controllers;

import com.kidsg.backend.payload.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final List<CategoryDto> categories = new ArrayList<>();
    private final List<ProductDto> products = new ArrayList<>();
    private final List<StoreDto> stores = new ArrayList<>();

    public CatalogController() {
        initData();
    }

    private void initData() {
        // Categories
        categories.add(new CategoryDto("cat_stationery", "Stationery", "stationery", "notebooks", 1, true));
        categories.add(new CategoryDto("cat_art_craft", "Arts & Crafts", "art-craft", "art_craft", 2, true));
        categories.add(new CategoryDto("cat_school_kits", "School Kits", "school-kits", "exam_essentials", 3, true));
        categories.add(new CategoryDto("cat_school_bags", "School Bags", "school-bags", "bags_accessories", 4, true));
        categories.add(new CategoryDto("cat_water_bottles", "Water Bottles", "water-bottles", "water_bottles", 5, true));
        categories.add(new CategoryDto("cat_lunch_boxes", "Lunch Boxes", "lunch-boxes", "lunch_boxes", 6, true));

        // Products
        products.add(new ProductDto(
                "prod_classmate_nb_6pk",
                "Classmate Pulse Spiral Notebook (Pack of 6)",
                "classmate-pulse-spiral-notebook-pack-of-6",
                "Single line ruling, 300 pages, ozone treated chlorine-free paper.",
                "Classmate",
                "cat_stationery",
                "Stationery",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
                340.0,
                420.0,
                19,
                45,
                "pack",
                "Middle & High School",
                true,
                true,
                Map.of("Pages", "300 per book", "Cover", "Polypropylene waterproof")
        ));

        products.add(new ProductDto(
                "prod_camlin_brush_pens_24",
                "Camlin Kokuyo Brush Pens (24 Shades)",
                "camlin-brush-pens-24-shades",
                "Flexible brush tip for calligraphy, fine art, lettering & blending.",
                "Camlin",
                "cat_art_craft",
                "Arts & Crafts",
                "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=600",
                380.0,
                450.0,
                15,
                60,
                "set",
                "All Grades",
                true,
                true,
                Map.of("Shades", "24 Vibrant Colors", "Tip", "Fine Flexible Brush")
        ));

        products.add(new ProductDto(
                "prod_grade7_complete_kit",
                "Grade 7 Complete Academic Starter Pack",
                "grade-7-complete-academic-starter-pack",
                "Includes 12 notebooks, geometry set, pens, highlighters and book covers.",
                "KidsG Official",
                "cat_school_kits",
                "School Kits",
                "https://images.unsplash.com/photo-1452860606245-08befc0ff44b?w=600",
                1299.0,
                1699.0,
                23,
                30,
                "kit",
                "Class 7",
                true,
                true,
                Map.of("Grade", "Class 7", "Board", "CBSE / ICSE / State")
        ));

        products.add(new ProductDto(
                "prod_skybags_ergo_backpack",
                "Skybags Campus Ergo 32L Backpack",
                "skybags-campus-ergo-backpack",
                "Triple compartment with rain cover, ergonomic lumbar support cushions.",
                "Skybags",
                "cat_school_bags",
                "School Bags",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600",
                1499.0,
                2199.0,
                31,
                25,
                "piece",
                "Middle & High School",
                true,
                true,
                Map.of("Capacity", "32 Litres", "Rain Cover", "Included")
        ));

        products.add(new ProductDto(
                "prod_milton_thermo_750",
                "Milton Kool Steel Insulated Flask 750ml",
                "milton-kool-steel-750ml",
                "Keeps liquids chilled for 24 hours. Leak-proof flip-top spout with handle.",
                "Milton",
                "cat_water_bottles",
                "Water Bottles",
                "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600",
                599.0,
                750.0,
                20,
                40,
                "piece",
                "All Grades",
                true,
                true,
                Map.of("Capacity", "750 ml", "Material", "SS304 Food Grade Stainless Steel")
        ));

        // Nearby store
        stores.add(new StoreDto(
                "store_blr_koramangala",
                "KidsG Hub Koramangala",
                "80 Feet Rd, 4th Block, Koramangala",
                "Bengaluru",
                12.9352,
                77.6245,
                "+91 98765 43210",
                6.0,
                true,
                0.8,
                15,
                true,
                4.9
        ));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<ProductsResponseDto>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        List<ProductDto> filtered = products.stream()
                .filter(p -> category == null || category.isBlank() || p.getCategoryId().equalsIgnoreCase(category))
                .filter(p -> search == null || search.isBlank() ||
                        p.getName().toLowerCase().contains(search.toLowerCase()) ||
                        p.getBrand().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        ProductsResponseDto.PaginationDto pagination = new ProductsResponseDto.PaginationDto(
                filtered.size(), 1, 20, 1
        );

        return ResponseEntity.ok(ApiResponse.success(new ProductsResponseDto(filtered, pagination)));
    }

    @GetMapping("/products/featured")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getFeaturedProducts() {
        List<ProductDto> featured = products.stream()
                .filter(ProductDto::isFeatured)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(featured));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable String id) {
        return products.stream()
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst()
                .map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Product not found", "NOT_FOUND")));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponseDto>> search(@RequestParam String q) {
        String queryLower = q.toLowerCase();
        List<ProductDto> matchingProducts = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(queryLower) || p.getBrand().toLowerCase().contains(queryLower))
                .collect(Collectors.toList());

        List<CategoryDto> matchingCategories = categories.stream()
                .filter(c -> c.getName().toLowerCase().contains(queryLower))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(new SearchResponseDto(q, matchingProducts, matchingCategories)));
    }

    @GetMapping("/stores/nearby")
    public ResponseEntity<ApiResponse<List<StoreDto>>> getNearbyStores() {
        return ResponseEntity.ok(ApiResponse.success(stores));
    }
}
