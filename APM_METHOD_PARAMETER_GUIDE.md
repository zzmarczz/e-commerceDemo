# APM Method Parameter Data Collectors - Complete Guide

## 🎯 Capturing Revenue from Method Parameters

This guide shows how to configure APM to capture revenue data **directly from method parameters** using **object navigation**.

---

## 📍 Updated Method Signature

**Service:** `api-gateway` (port 8080)  
**Class:** `com.demo.gateway.controller.GatewayController`  
**Method:** `checkout`

**NEW Method Signature:**
```java
public Mono<ResponseEntity<String>> checkout(
    @RequestBody CheckoutRequest checkoutRequest,     // Parameter 0 ← NAVIGATE THIS!
    @RequestHeader String sessionId,                  // Parameter 1
    @RequestHeader String journeyId                   // Parameter 2
)
```

**CheckoutRequest Object Structure:**
```java
CheckoutRequest {
    String userId
    List<CheckoutItem> items
    Double totalAmount      ← APM TARGET!
    Integer itemCount       ← APM TARGET!
}
```

---

## 🎯 **APM Configuration: AppDynamics**

### **Data Collector 1: Total Amount (Method Parameter Navigation)**

**Step 1: Create Data Collector**
```
Applications → Configuration → Instrumentation → Data Collectors → Add
```

**Step 2: Basic Settings**
```
Name: Revenue_TotalAmount_Param
Display Name: order_total_param
Data Collector Type: Method Invocation Data Collector
```

**Step 3: Method Specification**
```
Class Name: com.demo.gateway.controller.GatewayController
            ↑ Must be exact (including package)

Method Name: checkout
             ↑ Case-sensitive

Match Condition: Specific Method
```

**Step 4: Data Collection (KEY PART!)**
```
Gather Data From: Method Parameter
                   ↑ Choose this option

Parameter Index: 0
                 ↑ checkoutRequest is the FIRST parameter (0-based)

Use Accessor Chain (Object Navigation): YES
                                        ↑ Enable this!

Accessor Chain: getTotalAmount
                ↑ Navigate to checkoutRequest.getTotalAmount()

Data Type: Double
           ↑ Match the return type of getTotalAmount()

Collection Strategy: Collect on Method Entry
                     ↑ Data is available when method starts

Transform Value: None
```

**Visual Representation:**
```
Method:    checkout(checkoutRequest, sessionId, journeyId)
                          ↓
Parameter Index 0:   checkoutRequest
                          ↓
Navigate (call):     getTotalAmount()
                          ↓
Result:              179.98 (Double)
                          ↓
APM captures:        order_total_param = 179.98
```

**Click: Save**

---

### **Data Collector 2: Item Count**

**Repeat above with:**
```
Name: Revenue_ItemCount_Param
Display Name: item_count_param
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Gather From: Method Parameter
Parameter Index: 0
Accessor Chain: getItemCount
                ↑ Navigate to checkoutRequest.getItemCount()
Data Type: Integer
Collection Strategy: Collect on Method Entry
```

---

### **Data Collector 3: User ID**

```
Name: Revenue_UserID_Param
Display Name: user_id_param
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Gather From: Method Parameter
Parameter Index: 0
Accessor Chain: getUserId
                ↑ Navigate to checkoutRequest.getUserId()
Data Type: String
Collection Strategy: Collect on Method Entry
```

---

### **Data Collector 4: Session ID (No Navigation)**

```
Name: Revenue_SessionID
Display Name: session_id
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Gather From: Method Parameter
Parameter Index: 1
                 ↑ sessionId is the 2nd parameter
Accessor Chain: (leave empty)
                ↑ No navigation needed, it's already a String
Data Type: String
Collection Strategy: Collect on Method Entry
```

---

### **Data Collector 5: Journey ID (No Navigation)**

```
Name: Revenue_JourneyID
Display Name: journey_id
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Gather From: Method Parameter
Parameter Index: 2
                 ↑ journeyId is the 3rd parameter
Accessor Chain: (leave empty)
Data Type: String
Collection Strategy: Collect on Method Entry
```

---

## 📋 **Parameter Index Reference**

For this method:
```java
public Mono<ResponseEntity<String>> checkout(
    @RequestBody CheckoutRequest checkoutRequest,     // Index 0
    @RequestHeader String sessionId,                  // Index 1  
    @RequestHeader String journeyId                   // Index 2
)
```

| Parameter | Index | Type | Navigation Needed? |
|-----------|-------|------|--------------------|
| `checkoutRequest` | 0 | CheckoutRequest | ✅ Yes → getTotalAmount(), getItemCount(), getUserId() |
| `sessionId` | 1 | String | ❌ No (already simple type) |
| `journeyId` | 2 | String | ❌ No (already simple type) |

---

## 🎯 **Object Navigation Syntax**

### **Example 1: Single Level Navigation**

```
Parameter Index: 0 (checkoutRequest)
Accessor Chain: getTotalAmount
Result: checkoutRequest.getTotalAmount() → 179.98
```

### **Example 2: Multi-Level Navigation**

If you want to navigate deeper:
```
Parameter Index: 0 (checkoutRequest)
Accessor Chain: getItems → get(0) → getPrice
Result: checkoutRequest.getItems().get(0).getPrice() → 99.99
```

### **Example 3: List Size**

```
Parameter Index: 0 (checkoutRequest)
Accessor Chain: getItems → size
Result: checkoutRequest.getItems().size() → 2
```

---

## 🔧 **AppDynamics UI: Step-by-Step Screenshots Guide**

### **Screen 1: Create Data Collector**

```
┌────────────────────────────────────────────────────────┐
│ Add Data Collector                                     │
├────────────────────────────────────────────────────────┤
│                                                        │
│ Name: Revenue_TotalAmount_Param                        │
│                                                        │
│ Display Name: order_total_param                        │
│                                                        │
│ Data Collector Type:                                   │
│ ○ HTTP Data Collector                                  │
│ ● Method Invocation Data Collector    ← SELECT THIS   │
│ ○ Session Attribute                                    │
│                                                        │
│ [Next]                                                 │
└────────────────────────────────────────────────────────┘
```

---

### **Screen 2: Method Specification**

```
┌────────────────────────────────────────────────────────┐
│ Specify Method                                         │
├────────────────────────────────────────────────────────┤
│                                                        │
│ Class Name:                                            │
│ [com.demo.gateway.controller.GatewayController_____]   │
│   ↑ Type EXACTLY as shown                             │
│                                                        │
│ Method Name:                                           │
│ [checkout_________________________________________]    │
│   ↑ Case-sensitive                                     │
│                                                        │
│ Match Condition:                                       │
│ ● Specific Method                                      │
│ ○ All Methods                                          │
│                                                        │
│ [Next]                                                 │
└────────────────────────────────────────────────────────┘
```

---

### **Screen 3: Data Collection (MOST IMPORTANT!)**

```
┌────────────────────────────────────────────────────────┐
│ Specify Data to Collect                                │
├────────────────────────────────────────────────────────┤
│                                                        │
│ Gather Data From:                                      │
│ ○ Return Value                                         │
│ ● Method Parameter            ← SELECT THIS            │
│ ○ Local Variable                                       │
│ ○ Field Value                                          │
│                                                        │
│ Parameter Index: [0___]       ← checkoutRequest        │
│   ↑ 0-based index                                      │
│                                                        │
│ ☑ Use Accessor Chain          ← CHECK THIS BOX        │
│                                                        │
│ Accessor Chain:                                        │
│ [getTotalAmount_________________________________]      │
│   ↑ Method name to call on the parameter              │
│                                                        │
│ Data Type:                                             │
│ ○ String                                               │
│ ○ Integer                                              │
│ ● Double                      ← SELECT THIS            │
│ ○ Long                                                 │
│ ○ Boolean                                              │
│                                                        │
│ Collection Strategy:                                   │
│ ● Collect on Method Entry     ← SELECT THIS            │
│ ○ Collect on Method Exit                               │
│                                                        │
│ [Save]                                                 │
└────────────────────────────────────────────────────────┘
```

---

## ✅ **Verify Configuration**

### **Step 1: Assign to Business Transaction**

```
1. Configuration → Instrumentation → Transaction Detection
2. Find tier: api-gateway
3. Find Business Transaction: POST /api/orders/checkout
4. Edit → Data Collectors section
5. Add:
   ✅ Revenue_TotalAmount_Param
   ✅ Revenue_ItemCount_Param
   ✅ Revenue_UserID_Param
   ✅ Revenue_SessionID
   ✅ Revenue_JourneyID
6. Save
```

---

### **Step 2: Enable in Analytics**

```
Analytics → Configuration → Transaction Analytics
→ Add Fields:
   - order_total_param (Source: Data Collector, Type: Double)
   - item_count_param (Source: Data Collector, Type: Integer)
   - user_id_param (Source: Data Collector, Type: String)
   - session_id (Source: Data Collector, Type: String)
   - journey_id (Source: Data Collector, Type: String)
```

---

### **Step 3: Deploy Code**

```bash
cd ~/e-commerceDemo
git pull
cd api-gateway
mvn clean package -DskipTests
cd ..
./stop-all.sh
./start-with-load.sh
# Wait 60 seconds
```

---

### **Step 4: Check Logs**

```bash
tail -f logs/api-gateway.log | grep APM_REVENUE_PARAM
```

**You should see:**
```
APM_REVENUE_PARAM: userId=user95, totalAmount=179.98, itemCount=2, sessionId=loadgen-session-..., journeyId=loadgen-journey-...
```

---

### **Step 5: Wait and Verify in APM**

**Wait 5-10 minutes**, then:

```
1. Applications → Business Transactions
2. POST /api/orders/checkout
3. View → Transaction Snapshots
4. Click on a RECENT snapshot
5. Scroll to "Data Collectors" section
6. You should see:
   ✅ order_total_param: 179.98
   ✅ item_count_param: 2
   ✅ user_id_param: user95
   ✅ session_id: loadgen-session-...
   ✅ journey_id: loadgen-journey-...
```

---

## 📊 **Query Revenue Data**

### **Analytics Query:**

```sql
SELECT 
  order_total_param as revenue,
  item_count_param as items,
  user_id_param as user,
  session_id,
  journey_id
FROM transactions
WHERE segments.businessTransaction.name = 'POST /api/orders/checkout'
  AND order_total_param IS NOT NULL
LIMIT 20
```

### **Aggregate Revenue:**

```sql
SELECT 
  SUM(order_total_param) as total_revenue,
  COUNT(*) as total_orders,
  AVG(order_total_param) as avg_order_value
FROM transactions
WHERE segments.businessTransaction.name = 'POST /api/orders/checkout'
  AND order_total_param IS NOT NULL
  AND timestamp > NOW() - INTERVAL '1 hour'
```

**Expected Results:**
```
total_revenue: $4,499.50
total_orders: 25
avg_order_value: $179.98
```

---

## 🔧 **Troubleshooting**

### **Issue 1: "Parameter Index out of bounds"**

**Cause:** Wrong parameter index

**Fix:**
```
Count parameters from 0:
  checkout(CheckoutRequest checkoutRequest,  ← Index 0
           String sessionId,                 ← Index 1
           String journeyId)                 ← Index 2
```

---

### **Issue 2: "Accessor Chain method not found"**

**Cause:** Method name typo or doesn't exist

**Fix:**
```bash
# Verify method exists in DTO
cd ~/e-commerceDemo/api-gateway
grep -n "getTotalAmount" src/main/java/com/demo/gateway/dto/CheckoutRequest.java

# Output should show the method definition
```

**Common mistakes:**
- ❌ `getTotalAmount()` (don't include parentheses!)
- ✅ `getTotalAmount` (correct)
- ❌ `gettotalamount` (wrong case)
- ✅ `getTotalAmount` (correct case)

---

### **Issue 3: "Data Collector shows null"**

**Causes:**
1. DTO not calculating totals
2. JSON parsing failed
3. Items list is empty

**Debug:**
```bash
# Check logs
tail -f logs/api-gateway.log | grep APM_REVENUE_PARAM

# If you see: totalAmount=null, itemCount=null
# The DTO calculation is not working

# Check if items are present
tail -f logs/api-gateway.log | grep "items="
```

**Fix:** Make sure CheckoutRequest.setItems() calls calculateTotals()

---

### **Issue 4: "Method not being instrumented"**

**Cause:** Class/method name mismatch

**Debug:**
```bash
# Get full class name
cd ~/e-commerceDemo/api-gateway
grep -n "^package\|^public class" src/main/java/com/demo/gateway/controller/GatewayController.java

# Output:
# package com.demo.gateway.controller;
# public class GatewayController

# Full name: com.demo.gateway.controller.GatewayController
```

---

## 🎯 **Why Method Parameters Are Better**

| Aspect | Method Parameters | Local Variables |
|--------|-------------------|-----------------|
| **Capture Point** | ✅ Method entry (immediate) | ⚠️ Method exit (delayed) |
| **Reliability** | ✅ Always available | ⚠️ Might be null if exit early |
| **Performance** | ✅ Slightly faster | ⚠️ Slightly slower |
| **Object Navigation** | ✅ Easy (use accessors) | ❌ Not possible |
| **Setup** | ⚠️ Requires DTO classes | ✅ Works with any variable |

---

## 📋 **Complete Configuration Checklist**

- [ ] Created CheckoutRequest DTO class
- [ ] Created CheckoutItem DTO class
- [ ] Modified GatewayController to accept CheckoutRequest parameter
- [ ] Deployed code (git pull, rebuild, restart)
- [ ] Verified logs show APM_REVENUE_PARAM
- [ ] Created Data Collector: order_total_param
  - [ ] Class: com.demo.gateway.controller.GatewayController
  - [ ] Method: checkout
  - [ ] Gather From: Method Parameter
  - [ ] Parameter Index: 0
  - [ ] Accessor Chain: getTotalAmount
  - [ ] Data Type: Double
- [ ] Created Data Collector: item_count_param
  - [ ] Parameter Index: 0
  - [ ] Accessor Chain: getItemCount
  - [ ] Data Type: Integer
- [ ] Created Data Collector: user_id_param
  - [ ] Parameter Index: 0
  - [ ] Accessor Chain: getUserId
  - [ ] Data Type: String
- [ ] Created Data Collector: session_id
  - [ ] Parameter Index: 1
  - [ ] No accessor chain
  - [ ] Data Type: String
- [ ] Created Data Collector: journey_id
  - [ ] Parameter Index: 2
  - [ ] No accessor chain
  - [ ] Data Type: String
- [ ] Assigned all collectors to Business Transaction
- [ ] Enabled in Analytics
- [ ] Waited 5-10 minutes
- [ ] Generated new transactions
- [ ] Verified data in transaction snapshots
- [ ] Verified data in analytics queries

---

## 🎉 **Success Criteria**

**You're successful when:**

1. ✅ Logs show: `APM_REVENUE_PARAM: userId=..., totalAmount=179.98, itemCount=2`
2. ✅ Transaction Snapshot shows all 5 data collectors with values
3. ✅ Analytics query returns revenue data
4. ✅ Can create revenue dashboard with real-time data

---

## 📊 **Next: Create Revenue Dashboard**

Once data collectors work, create this dashboard:

**Widget 1: Total Revenue (KPI)**
```sql
SELECT SUM(order_total_param) as value
FROM transactions
WHERE segments.businessTransaction.name = 'POST /api/orders/checkout'
AND timestamp > NOW() - INTERVAL '1 hour'
```

**Widget 2: Revenue Over Time (Line Chart)**
```sql
SELECT 
  DATE_TRUNC('minute', timestamp) as time,
  SUM(order_total_param) as revenue
FROM transactions
WHERE segments.businessTransaction.name = 'POST /api/orders/checkout'
GROUP BY time
ORDER BY time
```

**Widget 3: Top Users by Revenue (Table)**
```sql
SELECT 
  user_id_param as user,
  SUM(order_total_param) as total_spent,
  COUNT(*) as order_count,
  AVG(order_total_param) as avg_order
FROM transactions
WHERE segments.businessTransaction.name = 'POST /api/orders/checkout'
GROUP BY user_id_param
ORDER BY total_spent DESC
LIMIT 10
```

---

**Method parameter data collectors are the MOST RELIABLE way to capture revenue in AppDynamics!** ✅

