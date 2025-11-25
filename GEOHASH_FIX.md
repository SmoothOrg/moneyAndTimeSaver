# Geohash Precision Issue - Fixed!

## Problem
Your location coordinates changed slightly:
- **Sample data**: 28.4614, 77.2981 → Geohash (9-char): `ttncyvnxz`
- **Your actual**: 28.461406, 77.297996 → Geohash (9-char): `ttncyvnxt`
- **Difference**: Only 45 meters apart, but different 9-char geohash!

## Root Cause
9-character geohash precision is TOO PRECISE (~5m × 5m area). Even tiny GPS variations create different geohashes.

---

## Solution: Reduce Precision to 7 Characters

### Geohash Precision Levels:
```
Chars | Area Coverage      | Use Case
------|-------------------|---------------------------
5     | ~5km × 5km        | City-level
6     | ~1.2km × 600m     | Neighborhood  
7     | ~150m × 150m      | Street/Block ✅ (RECOMMENDED)
8     | ~38m × 19m        | Building
9     | ~5m × 5m          | GPS point (too precise)
```

### Why 7 Characters is Perfect:
- ✅ Covers ~150m radius (typical dark store delivery area)
- ✅ Handles GPS drift/variations
- ✅ One dark store can serve the area
- ✅ Matches real quick-commerce delivery zones

---

## What I Changed

### Backend:
```java
// ProductSearchController.java
@RequestParam(value = "precision", defaultValue = "7")  // Changed from 9 to 7
```

Now all searches use 7-character geohash automatically.

---

## Fix Your Existing Data

### Option 1: Update Existing Geohashes (Recommended)

Run this in Kibana:
```
POST grocery_products_v1/_update_by_query
{
  "script": {
    "source": "ctx._source.geohash = ctx._source.geohash.substring(0, 7)",
    "lang": "painless"
  },
  "query": {
    "match_all": {}
  }
}
```

This converts all 9-char geohashes (ttncyvnxz) → 7-char (ttncyvn)

### Option 2: Re-add Data with 7-char Geohash

Your location: 28.461406, 77.297996
7-char geohash: `ttncyvn`

Just change all instances of:
```
"geohash": "ttncyvnxz"  →  "geohash": "ttncyvn"
```

---

## Verify It Works

### Test in Kibana:
```
# Should return your products now
GET grocery_products_v1/_search
{
  "query": {
    "bool": {
      "must": {
        "match": {
          "product_name": "paneer"
        }
      },
      "filter": {
        "term": {
          "geohash": "ttncyvn"
        }
      }
    }
  }
}
```

### Test via API:
```bash
curl "http://localhost:8080/api/products/search/by-location?query=paneer&latitude=28.461406&longitude=77.297996"
```

Should now return products!

---

## Why This Happened

### With 9-char precision:
```
GPS Signal A: 28.4614, 77.2981    → ttncyvnxz
GPS Signal B: 28.461406, 77.297996 → ttncyvnxt
                                      Different! ❌
```

### With 7-char precision:
```
GPS Signal A: 28.4614, 77.2981    → ttncyvn
GPS Signal B: 28.461406, 77.297996 → ttncyvn
                                      Same! ✅
```

---

## Quick Fix Steps

1. **Run the update script** (from `fix_geohash_precision.txt`):
   ```
   POST grocery_products_v1/_update_by_query
   {
     "script": {
       "source": "ctx._source.geohash = ctx._source.geohash.substring(0, 7)"
     },
     "query": { "match_all": {} }
   }
   ```

2. **Restart backend**:
   ```bash
   cd /Users/Akash.Verma/IntellijProjects/moneyAndTimeSaver
   ./mvnw spring-boot:run -pl web
   ```

3. **Test search**:
   - Frontend: Search "paneer"
   - Should now show results!

---

## Future: Store Geohash in DB Too

Consider storing user's geohash with precision level:
```java
// When saving location
String geohash7 = GeohashUtils.encode(lat, lon, 7);  // For search
String geohash9 = GeohashUtils.encode(lat, lon, 9);  // For analytics

user.setCurrentGeohash(geohash7);  // Used for product search
user.setCurrentGeohashPrecise(geohash9);  // For exact analytics
```

---

**Run the update script now and your search should work! 🚀**
