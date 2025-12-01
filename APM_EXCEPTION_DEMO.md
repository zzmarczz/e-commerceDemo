# APM Exception Detection Demo - NullPointerException

## 🎯 Purpose

This demonstrates how APM detects **real application crashes** (NullPointerException) vs **business validations** (HTTP 400 errors).

---

## 🚨 **Intentional Bug Introduced**

### **What:** NullPointerException when adding "Monitor" to cart

### **Where:**
```java
File: cart-service/src/main/java/com/demo/cart/controller/CartController.java
Lines: ~60-64
```

### **Code:**
```java
// BUG: Simulate a critical application error for APM demo (Monitor product)
if (request.getProductName().equalsIgnoreCase("Monitor")) {
    // Intentionally trigger NullPointerException
    String nullString = null;
    // This will throw: java.lang.NullPointerException
    int length = nullString.length();
}
```

---

## 🧪 **How to Trigger the Error**

### **Method 1: Using the UI (Easiest)**

1. Open browser: `http://3.10.224.202:3000`
2. Navigate to **Products** page
3. Find **"Monitor"** product (27-inch 4K monitor - $399.99)
4. Click **"Add to Cart"** button
5. ⚠️ **Error appears immediately!**

**Frontend Shows:**
```
❌ Failed to add to cart: Failed to add to cart
```

### **Method 2: Using cURL**

```bash
curl -X POST http://localhost:8080/api/cart/testuser/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 4,
    "productName": "Monitor",
    "price": 399.99,
    "quantity": 1
  }'
```

**Response:**
```json
{
  "timestamp": "2025-12-01T...",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/cart/testuser/items"
}
```

### **Method 3: Load Generator (Continuous Errors)**

```bash
# Generate mixed load including Monitor errors
./start-with-load.sh
```

---

## 📊 **What Your APM Will Detect**

### **1. Exception Details:**
```
Exception Type: java.lang.NullPointerException
Exception Message: Cannot invoke "String.length()" because "nullString" is null
```

### **2. Full Stack Trace:**
```
java.lang.NullPointerException: Cannot invoke "String.length()" because "nullString" is null
    at com.demo.cart.controller.CartController.addToCart(CartController.java:63)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(...)
    at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(...)
    at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(...)
    at org.springframework.web.servlet.DispatcherServlet.doDispatch(...)
    ...
```

### **3. Transaction Metrics:**
- **Status:** Failed ❌
- **HTTP Code:** 500
- **Response Time:** ~few milliseconds
- **Service:** CartService
- **Endpoint:** POST /api/cart/{userId}/items
- **Error Rate:** 100% for Monitor products

### **4. Service Map:**
```
Frontend → API Gateway → CartService [EXCEPTION!]
                            ↑
                      NullPointerException
```

### **5. Log Entries:**
```
ERROR [...] o.a.c.c.C.[.[.[/].[dispatcherServlet] : Servlet.service() for servlet [dispatcherServlet] threw exception
java.lang.NullPointerException: Cannot invoke "String.length()" because "nullString" is null
    at com.demo.cart.controller.CartController.addToCart(CartController.java:63)
    ...
```

---

## 🔍 **APM Analysis Comparison**

### **Monitor Error (NullPointerException) - This is REAL:**

| Metric | Value |
|--------|-------|
| **Error Type** | NullPointerException (Runtime Exception) |
| **HTTP Status** | 500 Internal Server Error |
| **Category** | **🔴 CRITICAL APPLICATION FAILURE** |
| **Stack Trace** | Full Java exception chain |
| **Root Cause** | Code bug - null pointer dereference |
| **Alert Level** | 🚨 CRITICAL - Immediate action required |
| **Impact** | Service unavailable for Monitor purchases |
| **Fix Required** | YES - Code change needed |

### **Keyboard Error (Business Validation) - This is EXPECTED:**

| Metric | Value |
|--------|-------|
| **Error Type** | Business Rule Validation |
| **HTTP Status** | 400 Bad Request |
| **Category** | **🟡 CLIENT ERROR / VALIDATION** |
| **Stack Trace** | None (clean response) |
| **Root Cause** | User exceeded quota (5 keyboards max) |
| **Alert Level** | ℹ️ INFO - Normal business logic |
| **Impact** | User notified, can adjust quantity |
| **Fix Required** | NO - Working as designed |

---

## 🎓 **Demo Script for APM Presentation**

### **Part 1: Show Both Error Types**

**Step 1 - Keyboard Validation (Expected):**
```
1. Add 6 keyboards to cart
2. Show in APM: HTTP 400, clean error message, no stack trace
3. Say: "This is a business rule - working perfectly"
```

**Step 2 - Monitor Bug (Critical):**
```
1. Add 1 monitor to cart
2. Show in APM: HTTP 500, NullPointerException, full stack trace
3. Say: "THIS is a real application failure - needs immediate fix"
```

### **Part 2: Highlight APM Features**

**Show APM capabilities:**
- ✅ Exception class and message
- ✅ Exact line number in code (line 63)
- ✅ Full stack trace for debugging
- ✅ Affected service (CartService)
- ✅ Request details that triggered it
- ✅ Error rate spike when triggered
- ✅ Service health status change

### **Part 3: Compare Side-by-Side**

```
Left Monitor:  Keyboard Errors (400s) - Green/Yellow - "Business as usual"
Right Monitor: Monitor Errors (500s)  - Red - "CRITICAL - All hands on deck!"
```

---

## 🔧 **How to Fix the Bug**

### **Manual Fix:**

1. **Open the file:**
   ```bash
   cart-service/src/main/java/com/demo/cart/controller/CartController.java
   ```

2. **Find this block (around line 58-64):**
   ```java
   // BUG: Simulate a critical application error for APM demo (Monitor product)
   if (request.getProductName().equalsIgnoreCase("Monitor")) {
       // Intentionally trigger NullPointerException
       String nullString = null;
       int length = nullString.length();
   }
   ```

3. **Delete the entire if block** (6 lines)

4. **Rebuild and restart:**
   ```bash
   cd cart-service
   mvn clean package -DskipTests
   cd ..
   ./stop-all.sh
   ./start-with-ui.sh
   ```

### **Quick Fix Script (Future Enhancement):**

```bash
# Could create: fix-monitor-bug.sh
cd cart-service/src/main/java/com/demo/cart/controller
sed -i '/BUG: Simulate a critical application error/,/^        }/d' CartController.java
cd ~/e-commerceDemo
cd cart-service && mvn clean package -DskipTests
cd .. && ./stop-all.sh && ./start-with-ui.sh
```

---

## 📋 **APM Error Categories - Summary**

| Product | Error Type | HTTP | APM Severity | Action Needed |
|---------|-----------|------|--------------|---------------|
| **Monitor** | NullPointerException | 500 | 🔴 CRITICAL | Fix code bug |
| **Keyboard (6+)** | Business Validation | 400 | 🟡 INFO | None (expected) |
| **Mouse** | Normal | 200 | 🟢 SUCCESS | None |
| **Laptop** | Normal | 200 | 🟢 SUCCESS | None |
| **Headphones** | Normal | 200 | 🟢 SUCCESS | None |

---

## 🚀 **Deploy to AWS EC2**

```bash
cd ~/e-commerceDemo
git pull
cd cart-service
mvn clean package -DskipTests
cd ..
./stop-all.sh
./start-with-ui.sh
```

*Wait 60 seconds for services to start*

---

## ✅ **Verification Steps**

### **1. Test the Error:**
```bash
curl -v -X POST http://localhost:8080/api/cart/testuser/items \
  -H "Content-Type: application/json" \
  -d '{"productId":4,"productName":"Monitor","price":399.99,"quantity":1}'
```

**Expected:** HTTP 500 response

### **2. Check Logs:**
```bash
tail -50 logs/cart-service.log | grep -i "NullPointer"
```

**Expected:** Stack trace showing NullPointerException at line 63

### **3. Verify APM:**
- Open APM dashboard
- Check error rate spike
- View exception details
- Confirm stack trace visible

### **4. Test Other Products (Should Work):**
```bash
# Mouse - should work ✅
curl -X POST http://localhost:8080/api/cart/testuser/items \
  -H "Content-Type: application/json" \
  -d '{"productId":2,"productName":"Mouse","price":29.99,"quantity":1}'

# Expected: HTTP 200 success
```

---

## 📊 **Expected APM Metrics**

When triggering Monitor error repeatedly:

```
Error Rate:         Monitor endpoint: 100% ❌
                   Other endpoints: 0% ✅

Response Times:     Monitor: ~5ms (fast failure)
                   Others: normal

Exception Count:    Spikes when Monitor added

Service Health:     CartService: Degraded/Critical
                   Others: Healthy

Transaction Flow:   Monitor: Broken at CartService
                   Others: Complete successfully
```

---

## 💡 **Key Takeaways for APM Demo**

1. **APM distinguishes error types:**
   - Runtime exceptions (500) = Critical
   - Validation errors (400) = Expected

2. **Stack traces matter:**
   - NullPointer: Full trace to exact line
   - Validation: No trace (not a bug)

3. **Actionable insights:**
   - APM points to exact code location
   - Line 63 in CartController
   - Can debug immediately

4. **Business impact:**
   - Monitor sales: Blocked ❌
   - Other products: Unaffected ✅
   - Partial service degradation detected

---

## 🎯 **Perfect Demo Flow**

```
1. Show normal operations (Mouse, Laptop) - All green
2. Show keyboard limit (6 keyboards) - Yellow warning, expected
3. Show monitor bug - RED ALERT! Exception! Stack trace!
4. Highlight APM detection capabilities
5. Show how to locate bug (line 63)
6. (Optional) Fix live and redeploy
7. Verify fix in APM (errors stop)
```

---

## 📝 **Summary**

✅ **NullPointerException introduced in CartService**  
✅ **Triggered by adding Monitor product**  
✅ **APM will detect with full stack traces**  
✅ **Demonstrates real vs expected errors**  
✅ **Easy to trigger and demonstrate**  
✅ **Easy to fix when demo complete**  

**This showcases APM's true value: Detecting and diagnosing critical application failures!** 🚨

