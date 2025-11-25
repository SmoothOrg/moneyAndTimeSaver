# How to Add Sample Data to Elasticsearch

## Method 1: Using Kibana Dev Tools (Recommended)

1. **Open Kibana**: http://localhost:5601/app/dev_tools#/console
2. **Copy and paste** commands from `sample_data_faridabad.txt`
3. **Run each command** (or select all and click ▶ Play button)

## Method 2: Using cURL

```bash
# Set your Elasticsearch URL
ES_URL="http://localhost:9200"

# Add Amul Paneer on Blinkit
curl -X POST "$ES_URL/grocery_products_v1/_doc/blinkit_amul_paneer_200g" \
  -H "Content-Type: application/json" \
  -d '{
    "platform": "blinkit",
    "product_id": "amul_paneer_200g",
    "product_name": "Amul Paneer",
    "brand_name": "Amul",
    "categories": "Dairy",
    "selling_price": 89.0,
    "mrp": 95.0,
    "availability": true,
    "geohash": "ttncyvnxz",
    "location": "28.4614,77.2981"
  }'

# Repeat for all products...
```

## Method 3: Bulk Insert (Fastest)

Create a file `bulk_insert.json`:
```json
{"index":{"_index":"grocery_products_v1","_id":"blinkit_amul_paneer_200g"}}
{"platform":"blinkit","product_name":"Amul Paneer","selling_price":89,"geohash":"ttncyvnxz","location":"28.4614,77.2981","availability":true}
{"index":{"_index":"grocery_products_v1","_id":"zepto_amul_paneer_200g"}}
{"platform":"zepto","product_name":"Amul Paneer","selling_price":92,"geohash":"ttncyvnxz","location":"28.4614,77.2981","availability":true}
```

Then run:
```bash
curl -X POST "http://localhost:9200/_bulk" \
  -H "Content-Type: application/x-ndjson" \
  --data-binary "@bulk_insert.json"
```

---

## Sample Data Included

### Products (15 total):
1. **Amul Paneer** (200g) - All platforms in stock
2. **Mother Dairy Paneer** (200g) - Zepto out of stock
3. **Amul Taaza Milk** (1L) - All platforms in stock
4. **Britannia Bread** (400g) - Blinkit out of stock
5. **Kissan Jam** (500g) - All platforms in stock
6. **Lay's Chips** (52g) - Different prices across platforms
7. **Coca Cola** (750ml) - Swiggy out of stock
8. **Tata Salt** (1kg) - All platforms in stock
9. **India Gate Rice** (5kg) - All platforms in stock
10. **Brown Eggs** (6pc) - Swiggy out of stock

### Price Variations:
- Some products cheaper on Zepto
- Some cheaper on Blinkit
- Some out of stock on specific platforms
- Perfect for testing cart comparison!

---

## Testing Cart Comparison

### Example Cart for Testing:
Add these products to cart:
1. Amul Paneer
2. Mother Dairy Paneer
3. Britannia Bread
4. Lay's Chips
5. Brown Eggs

**Expected Results:**
- **Zepto**: Should use Blinkit for Mother Dairy Paneer (out of stock on Zepto)
- **Blinkit**: Should use Zepto for Britannia Bread (out of stock on Blinkit)
- **Swiggy**: Should use other platforms for Brown Eggs (out of stock)
- **Best Deal**: Should calculate with delivery fees and show cheapest

---

## Verify Data Loaded

```bash
# Check count
curl "http://localhost:9200/grocery_products_v1/_count?q=geohash:ttncyvnxz"

# Should return: {"count": 27, ...} (9 products × 3 platforms)

# Test search
curl -X GET "http://localhost:8080/api/products/search/by-location?query=paneer&latitude=28.4614&longitude=77.2981"

# Should return Amul and Mother Dairy Paneer from all platforms
```

---

## Quick Test Commands

### Search Products:
```bash
# Paneer
curl "http://localhost:8080/api/products/search/by-location?query=paneer&latitude=28.4614&longitude=77.2981"

# Milk
curl "http://localhost:8080/api/products/search/by-location?query=milk&latitude=28.4614&longitude=77.2981"

# Bread
curl "http://localhost:8080/api/products/search/by-location?query=bread&latitude=28.4614&longitude=77.2981"

# All dairy
curl "http://localhost:8080/api/products/search/by-location?query=dairy&latitude=28.4614&longitude=77.2981"
```

---

## Adding More Products

Use this template:
```json
POST grocery_products_v1/_doc/{platform}_{product_id}
{
  "platform": "blinkit|zepto|swiggy_instamart",
  "product_id": "unique_product_id",
  "product_name": "Product Name",
  "brand_name": "Brand",
  "categories": "Category",
  "sub_categories": "Sub Category",
  "quantity": "200 g",
  "mrp": 100.0,
  "selling_price": 90.0,
  "discount_percent": 10.0,
  "availability": true,
  "stock_status": "in_stock",
  "pincode": "121002",
  "city": "faridabad",
  "geohash": "ttncyvnxz",
  "location": "28.4614,77.2981",
  "created_at": "2025-11-17T10:00:00Z",
  "updated_at": "2025-11-17T10:00:00Z"
}
```

---

**Now run the script and test your cart comparison feature! 🚀**
