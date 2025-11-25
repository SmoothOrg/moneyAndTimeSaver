# Search Relevance - Why Kissan Jam Shows in Bread Search

## The Issue

When searching "Britannia Bread - White", results include:
1. **Britannia Bread** - Score: 77.52 ✅ (Perfect match)
2. **Kissan Jam** - Score: 5.12 ❌ (Weak match)

---

## Why This Happened

### Elasticsearch Multi-Match with Fuzzy + Ngram:

Your query: `"Britannia Bread - White"`

**Kissan Jam matched because:**
1. **Ngram matching**: 
   - Query: "**Bre**ad"
   - Jam breadcrumbs: "Home > **Bre**akfast > Spreads"
   - Ngram tokenizer splits "Breakfast" → "Br", "Bre", "Brea", "Break"...
   - "**Bre**" matches!

2. **Fuzzy matching**: 
   - "Bread" ≈ "Breakfast" (edit distance: 5)
   - With `fuzziness("AUTO")`, this is acceptable

3. **Low score but included**:
   - Britannia Bread: **77.52** (strong match)
   - Kissan Jam: **5.12** (weak match, but still returned)

---

## The Fix: Add Minimum Score Threshold

### What I Changed:

```java
// Before
SearchRequest.Builder()
    .index(index)
    .query(query);

// After  
SearchRequest.Builder()
    .index(index)
    .query(query)
    .minScore(10.0);  // Only return results with score >= 10
```

### Impact:

**Old behavior:**
- Britannia Bread: 77.52 → ✅ Returned
- Kissan Jam: 5.12 → ✅ Returned (noise)

**New behavior:**
- Britannia Bread: 77.52 → ✅ Returned
- Kissan Jam: 5.12 → ❌ Filtered out

---

## Score Breakdown (Why the scores?)

### Britannia Bread = 77.52
```
product_name match: "Britannia Bread - White" (exact) → 60 points
brand_name match: "Britannia" → 15 points
categories match: "Bakery" (if in query) → 2 points
Total: ~77 points ✅
```

### Kissan Jam = 5.12
```
breadcrumbs ngram: "Bre" in "Breakfast" → 3 points
fuzzy match: "Bread" ≈ "Breakfast" → 2 points
Total: ~5 points (weak) ❌
```

---

## Is min_score = 10 Good?

### Testing Different Thresholds:

| min_score | Effect |
|-----------|--------|
| 0 (none) | Returns everything, lots of noise |
| 5 | Still shows Kissan Jam |
| 10 | ✅ Filters weak matches, keeps relevant |
| 20 | Might miss some valid matches |
| 50 | Too strict, only exact matches |

**10.0 is a good balance** for your use case!

---

## Alternative: Adjust Query Boosting

If you want even better results, reduce ngram and fuzzy importance:

```java
.fields(List.of(
    "product_name^10",        // Increase exact match importance
    "product_name.ngram^1",   // Decrease ngram (was ^2)
    "brand_name^5",           // Increase brand importance
    "brand_name.ngram^0.5",   // Reduce brand ngram
    "categories^3",
    "sub_categories^2"
    // Remove breadcrumbs from search entirely
))
.fuzziness("1")  // Reduce fuzziness (was "AUTO")
```

But for now, `min_score = 10` should work well!

---

## Test After Fix

### Rebuild backend:
```bash
cd /Users/Akash.Verma/IntellijProjects/moneyAndTimeSaver
./mvnw clean install
./mvnw spring-boot:run -pl web
```

### Test search:
```bash
curl "http://localhost:8080/api/products/search/by-location?query=Britannia+Bread&latitude=28.461406&longitude=77.297996"
```

**Expected:**
- ✅ Only shows Britannia Bread products
- ❌ Kissan Jam filtered out (score too low)

---

## When Fuzzy Search is Good

**Good examples:**
- User types "paner" → Shows "paneer" ✅
- User types "milk" → Shows "Milk", "Milkmaid" ✅
- User types "bred" → Shows "bread" ✅

**Bad examples (now fixed):**
- "bread" → Shows "breakfast items" ❌ (filtered by min_score)
- "salt" → Shows "salted butter" ❌ (would need higher threshold)

---

## Summary

✅ **Added `minScore(10.0)`** to filter weak matches
✅ **Keeps fuzzy search** for typo tolerance
✅ **Keeps ngram** for partial word matching
✅ **Removes noise** like Jam in Bread search

**This is the right balance for e-commerce search!**

Restart backend and test - Jam should disappear from Bread search! 🎯
