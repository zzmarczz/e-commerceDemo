# API Gateway Error Handling - APM Demo Guide

## 🎯 Overview

The API Gateway now implements **production-grade error handling** that properly categorizes errors in your APM tool.

---

## ❌ **Before the Fix - What APM Showed:**

### Problem:
When triggering the keyboard limit (business rule error):

```
❌ WebClientResponseException in FrameworkServlet
❌ "Unresolved handling" errors
❌ "Processing failures" across Gateway and CartService
❌ Categorized as: SYSTEM FAILURE / 500 ERROR
❌ Stack traces showing Spring internal filter chains
```

**APM Interpretation:** *"Critical system failure - service-side inefficiencies"*

**Reality:** The business logic was working perfectly! ✅

---

## ✅ **After the Fix - What APM Shows:**

### Improvement:
Same keyboard limit validation, but now:

```
✅ HTTP 400 Bad Request (clean response)
✅ Categorized as: CLIENT ERROR / VALIDATION FAILURE
✅ Original error message from Cart Service passed through
✅ No exception stack traces in API Gateway
✅ Proper error classification
```

**APM Interpretation:** *"Business validation error - user exceeded keyboard limit"*

**Reality:** Accurate! 🎯

---

## 🔧 **What Changed:**

### File: `api-gateway/src/main/java/com/demo/gateway/controller/GatewayController.java`

**Before:**
```java
@PostMapping("/cart/{userId}/items")
public Mono<String> addToCart(@PathVariable String userId, @RequestBody String requestBody) {
    return webClientBuilder.build()
            .post()
            .uri(cartServiceUrl + "/api/cart/" + userId + "/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class);
    // ❌ Exception propagates unhandled if backend returns 400/500
}
```

**After:**
```java
@PostMapping("/cart/{userId}/items")
public Mono<ResponseEntity<String>> addToCart(@PathVariable String userId, @RequestBody String requestBody) {
    return webClientBuilder.build()
            .post()
            .uri(cartServiceUrl + "/api/cart/" + userId + "/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .toEntity(String.class)
            .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
            .onErrorResume(WebClientResponseException.class, ex -> {
                // ✅ Gracefully handle errors - pass through original status and body
                return Mono.just(ResponseEntity
                        .status(ex.getStatusCode())
                        .body(ex.getResponseBodyAsString()));
            });
}
```

---

## 📊 **APM Error Classification:**

### Keyboard Limit Error (Business Rule):

| Metric | Before | After |
|--------|--------|-------|
| **HTTP Status** | 500 (misleading) | 400 (correct) |
| **Error Category** | System Failure | Client/Validation Error |
| **Stack Trace** | Full Spring exception chain | None (clean response) |
| **Error Rate Impact** | Counts as critical error | Counts as expected validation |
| **Alerting** | May trigger critical alerts | Informational only |

---

## 🧪 **How to Demo This to Your Team:**

### 1. **Show the Business Rule:**
```bash
# Point them to the code
cart-service/src/main/java/com/demo/cart/controller/CartController.java
# Line ~33: Maximum 5 keyboards per customer
```

### 2. **Trigger the Error:**
- Open: `http://your-ip:3000`
- Go to **Products**
- Try adding **6 keyboards** at once
- Error appears in UI: *"Maximum 5 keyboards per customer..."*

### 3. **Point Out in APM:**

**Before Fix:**
> "See this? The APM is showing a 'critical system failure' with FrameworkServlet exceptions. 
> But our business logic is working perfectly! This is just a validation rule."

**After Fix:**
> "Now look - APM correctly shows this as a 'client error' (HTTP 400). 
> The error message is clear, and there are no confusing stack traces. 
> This is how production systems should handle validation errors."

---

## 🎓 **Educational Value:**

### Key Points to Highlight:

1. **Error Categorization Matters:**
   - Not all errors are system failures
   - 4xx = Client/Business Rule errors (expected)
   - 5xx = Server/System errors (unexpected)

2. **API Gateway Best Practices:**
   - Don't let backend exceptions propagate unhandled
   - Pass through original status codes and error messages
   - Use `.onErrorResume()` for graceful error handling

3. **APM Accuracy:**
   - Proper error handling helps APM correctly identify issues
   - Reduces false alarms and alert fatigue
   - Makes troubleshooting easier

---

## 🔍 **What Your APM Will Now Track:**

### Error Metrics by Category:

**Client Errors (4xx):**
- Keyboard limit violations (400)
- Invalid product IDs (404)
- Malformed requests (400)
- *These are expected and won't trigger critical alerts*

**Server Errors (5xx):**
- Actual system failures
- Database connection issues
- Service unavailability
- *These will trigger critical alerts*

### Response Time Metrics:
- Still tracks all request latencies
- Slow order endpoint (when enabled) still visible
- No change to performance monitoring

---

## 🚀 **Deploy to AWS EC2:**

```bash
cd ~/e-commerceDemo
git pull
cd api-gateway
mvn clean package -DskipTests
cd ..
./stop-all.sh
./start-with-ui.sh
```

*Wait 60 seconds for services to start*

---

## ✅ **Verification:**

### Test the Fix:

1. **Trigger keyboard error:**
   ```bash
   curl -X POST http://localhost:8080/api/cart/testuser/items \
     -H "Content-Type: application/json" \
     -d '{"productId":3,"productName":"Keyboard","price":79.99,"quantity":6}'
   ```

2. **Expected Response:**
   ```json
   {
     "error": "Business Rule Violation",
     "message": "Maximum 5 keyboards per customer. You currently have 0 keyboard(s) in cart. Cannot add 6 more."
   }
   ```

3. **Check Gateway Logs:**
   ```bash
   tail -50 logs/api-gateway.log | grep -i "error"
   ```
   
   **Should NOT see:** ❌ WebClientResponseException stack traces  
   **Should see:** ✅ Clean request/response logging

---

## 📝 **Summary:**

✅ **Implemented production-grade error handling**  
✅ **APM now correctly categorizes errors**  
✅ **Business rule violations ≠ system failures**  
✅ **Cleaner logs and stack traces**  
✅ **Frontend receives proper error messages**  
✅ **More accurate alerting and monitoring**

This is how enterprise applications should handle errors! 🎉

