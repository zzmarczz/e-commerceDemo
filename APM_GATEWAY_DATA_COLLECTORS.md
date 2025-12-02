# APM Data Collectors for Gateway Revenue Tracking

## 🎯 Method-Based Revenue Capture from API Gateway

Since your APM monitors the **Gateway only**, we've added code to parse the Order Service response and extract revenue data into **local variables** that APM can capture.

---

## 📍 Target Method for Instrumentation

**Service:** `api-gateway` (port 8080)  
**Class:** `com.demo.gateway.controller.GatewayController`  
**Method:** `checkout`

**Method Signature:**
```java
public Mono<ResponseEntity<String>> checkout(
    @RequestBody String requestBody,
    @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
    @RequestHeader(value = "X-Journey-ID", required = false) String journeyId
)
```

**Local Variables to Capture (APM Data Collectors):**
- `orderId` (Long) - Order ID from response
- `orderValue` (Double) - Order total amount  
- `itemCount` (Integer) - Number of items
- `orderUserId` (String) - User ID from response
- `sessionId` (String) - Session ID from header
- `journeyId` (String) - Journey ID from header

---

## 🔧 How It Works

```java
// Inside GatewayController.checkout() method:

.map(response -> {
    String responseBody = response.getBody();
    
    // These LOCAL VARIABLES can be captured by APM!
    Long orderId = null;
    Double orderValue = null;
    Integer itemCount = null;
    String orderUserId = null;
    
    // Parse JSON response from Order Service
    JsonNode orderJson = objectMapper.readTree(responseBody);
    orderId = orderJson.get("id").asLong();
    orderValue = orderJson.get("totalAmount").asDouble();
    itemCount = orderJson.get("items").size();
    orderUserId = orderJson.get("userId").asText();
    
    // Log for additional visibility
    logger.info("APM_REVENUE_GATEWAY: orderId={}, orderValue={}, ...", 
                orderId, orderValue, ...);
    
    return builder.body(responseBody);
})
```

**APM can capture:**
1. Local variables (`orderId`, `orderValue`, `itemCount`)
2. Method parameters (`sessionId`, `journeyId`)
3. Log messages (`APM_REVENUE_GATEWAY`)

---

## 📋 APM Configuration by Tool

---

## 1️⃣ AppDynamics - Gateway Data Collectors

### **Method A: GUI Configuration (Easiest)**

**Step 1: Navigate to Data Collectors**
```
Applications 
→ [Your App]
→ Configuration 
→ Instrumentation 
→ Data Collectors 
→ Add
```

**Step 2: Create Local Variable Collectors**

**Collector 1: Order Value**
```
Name: Gateway Revenue - Order Value
Data Collector Type: Method Invocation Data Collector

Class Name: com.demo.gateway.controller.GatewayController
Method Name: checkout
Parameter Types: (leave empty or specify: String, String, String)

Data Gathered From: Local Variable
Variable Name: orderValue
Display Name: order_value
Data Type: DOUBLE
```

**Collector 2: Order ID**
```
Name: Gateway Revenue - Order ID
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Data Gathered From: Local Variable
Variable Name: orderId
Display Name: order_id
Data Type: LONG
```

**Collector 3: Item Count**
```
Name: Gateway Revenue - Item Count
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Data Gathered From: Local Variable
Variable Name: itemCount
Display Name: item_count
Data Type: INTEGER
```

**Collector 4: User ID**
```
Name: Gateway Revenue - User ID
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Data Gathered From: Local Variable
Variable Name: orderUserId
Display Name: user_id
Data Type: STRING
```

**Collector 5: Session ID**
```
Name: Gateway Revenue - Session ID
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Data Gathered From: Method Parameter
Parameter Index: 1    [sessionId parameter]
Display Name: session_id
Data Type: STRING
```

**Step 3: Assign to Business Transaction**
```
Business Transactions → POST /api/orders/checkout
→ Transaction Snapshots → Data Collectors
→ Select all collectors created above
```

**Step 4: Enable in Analytics**
```
Analytics → Configuration → Transaction Analytics
→ Add Fields:
  - order_value (Double)
  - order_id (Long)
  - item_count (Integer)
  - user_id (String)
  - session_id (String)
```

**Step 5: Verify**
```
1. Generate test orders (manually or load generator)
2. Wait 5-10 minutes
3. Go to: Business Transactions → POST /api/orders/checkout
4. Click on any recent transaction
5. Look for "Data Collectors" or "Custom Data" section
6. You should see: order_value, order_id, item_count
```

---

### **Method B: Log-Based Data Collector (Alternative)**

If local variable capture doesn't work, use the structured log:

```
Data Collector Type: Log Message
Logger Name: com.demo.gateway.controller.GatewayController
Log Level: INFO
Message Pattern: APM_REVENUE_GATEWAY: orderId=(\\d+), orderValue=([\\d.]+), itemCount=(\\d+)

Extract Fields:
- order_id: Capture group 1 (Long)
- order_value: Capture group 2 (Double)
- item_count: Capture group 3 (Integer)
```

---

### **Method C: Custom Match Rule (XML)**

For advanced users, create `gateway-revenue-collectors.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<custom-match-rules version="1.0">
  
  <match-rule>
    <rule-name>GatewayController.checkout</rule-name>
    <rule-description>Capture revenue from Gateway checkout response parsing</rule-description>
    
    <match-specification>
      <match-class>
        <class-name>com.demo.gateway.controller.GatewayController</class-name>
      </match-class>
      <match-method>
        <method-name>checkout</method-name>
      </match-method>
    </match-specification>
    
    <data-collector-list>
      
      <data-collector>
        <name>order_value</name>
        <gather-info>
          <gather-from>LOCAL_VARIABLE</gather-from>
          <variable-name>orderValue</variable-name>
        </gather-info>
      </data-collector>
      
      <data-collector>
        <name>order_id</name>
        <gather-info>
          <gather-from>LOCAL_VARIABLE</gather-from>
          <variable-name>orderId</variable-name>
        </gather-info>
      </data-collector>
      
      <data-collector>
        <name>item_count</name>
        <gather-info>
          <gather-from>LOCAL_VARIABLE</gather-from>
          <variable-name>itemCount</variable-name>
        </gather-info>
      </data-collector>
      
      <data-collector>
        <name>session_id</name>
        <gather-info>
          <gather-from>METHOD_PARAMETER</gather-from>
          <parameter-index>1</parameter-index>
        </gather-info>
      </data-collector>
      
    </data-collector-list>
  </match-rule>
  
</custom-match-rules>
```

---

## 2️⃣ Datadog - Gateway Instrumentation

### **Option A: DD Trace Annotations**

Add to `api-gateway/pom.xml`:

```xml
<dependency>
    <groupId>com.datadoghq</groupId>
    <artifactId>dd-trace-api</artifactId>
    <version>1.24.0</version>
</dependency>
```

Modify `GatewayController.java`:

```java
import datadog.trace.api.Trace;
import datadog.trace.api.interceptor.MutableSpan;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

@PostMapping("/orders/checkout")
@Trace(operationName = "gateway.checkout", resourceName = "POST /orders/checkout")
public Mono<ResponseEntity<String>> checkout(...) {
    return request
        .bodyValue(requestBody)
        .retrieve()
        .toEntity(String.class)
        .map(response -> {
            // ... parsing code ...
            
            // Add tags to Datadog span
            Span span = GlobalTracer.get().activeSpan();
            if (span != null) {
                span.setTag("order.id", orderId != null ? orderId : 0L);
                span.setTag("order.value", orderValue != null ? orderValue : 0.0);
                span.setTag("order.item_count", itemCount != null ? itemCount : 0);
                span.setTag("order.user_id", orderUserId != null ? orderUserId : "unknown");
            }
            
            return builder.body(responseBody);
        });
}
```

**Query in Datadog:**
```
service:api-gateway resource_name:"POST /orders/checkout" @order.value:*

# Aggregate revenue
SUM(@order.value)
AVG(@order.value)
COUNT(*)
```

---

### **Option B: Datadog Log Parsing**

Configure Datadog to parse the structured log:

```yaml
# In Datadog log pipeline
name: Gateway Revenue Parser
filter: service:api-gateway AND message:APM_REVENUE_GATEWAY

grok_parser:
  source: message
  grok:
    match_rules: APM_REVENUE_GATEWAY: orderId=%{NUMBER:order_id:long}, orderValue=%{NUMBER:order_value:double}, itemCount=%{NUMBER:item_count:integer}
```

---

## 3️⃣ New Relic - Gateway Custom Attributes

Add to `api-gateway/pom.xml`:

```xml
<dependency>
    <groupId>com.newrelic.agent.java</groupId>
    <artifactId>newrelic-api</artifactId>
    <version>8.7.0</version>
</dependency>
```

Modify `GatewayController.java`:

```java
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

@PostMapping("/orders/checkout")
@Trace(dispatcher = true)
public Mono<ResponseEntity<String>> checkout(...) {
    return request
        .bodyValue(requestBody)
        .retrieve()
        .toEntity(String.class)
        .map(response -> {
            // ... parsing code ...
            
            // Add custom attributes to New Relic transaction
            if (orderId != null) {
                NewRelic.addCustomParameter("order_id", orderId);
            }
            if (orderValue != null) {
                NewRelic.addCustomParameter("order_value", orderValue);
            }
            if (itemCount != null) {
                NewRelic.addCustomParameter("item_count", itemCount);
            }
            if (orderUserId != null) {
                NewRelic.addCustomParameter("user_id", orderUserId);
            }
            
            return builder.body(responseBody);
        });
}
```

**Query (NRQL):**
```sql
SELECT 
  sum(order_value) as 'Total Revenue',
  count(*) as 'Total Orders',
  average(order_value) as 'Avg Order Value'
FROM Transaction
WHERE appName = 'api-gateway'
  AND name LIKE '%checkout%'
  AND order_value IS NOT NULL
SINCE 1 hour ago
```

---

## 4️⃣ Elastic APM - Gateway Labels

Add to `api-gateway/pom.xml`:

```xml
<dependency>
    <groupId>co.elastic.apm</groupId>
    <artifactId>apm-agent-api</artifactId>
    <version>1.42.0</version>
</dependency>
```

Modify `GatewayController.java`:

```java
import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;

@PostMapping("/orders/checkout")
public Mono<ResponseEntity<String>> checkout(...) {
    return request
        .bodyValue(requestBody)
        .retrieve()
        .toEntity(String.class)
        .map(response -> {
            // ... parsing code ...
            
            // Add labels to Elastic transaction
            Transaction transaction = ElasticApm.currentTransaction();
            if (orderId != null) {
                transaction.setLabel("order_id", orderId.toString());
            }
            if (orderValue != null) {
                transaction.setLabel("order_value", orderValue);
            }
            if (itemCount != null) {
                transaction.setLabel("item_count", itemCount);
            }
            
            return builder.body(responseBody);
        });
}
```

---

## 5️⃣ Dynatrace - Gateway Request Attributes

**No code changes needed!** Dynatrace can capture local variables automatically.

**Step 1: Configure Request Attributes**

```
Settings 
→ Server-side service monitoring 
→ Request attributes 
→ Define a new request attribute
```

**For Order Value:**
```
Request attribute name: Gateway_Order_Value
Data type: Double
Data source: Java method → Local variable
Service: api-gateway
Class: com.demo.gateway.controller.GatewayController
Method: checkout
Variable name: orderValue
```

Repeat for `orderId`, `itemCount`, `orderUserId`.

---

## 📊 Deployment and Testing

### **Step 1: Deploy Changes**

```bash
cd ~/e-commerceDemo
git pull

# Rebuild Gateway with new JSON parsing code
cd api-gateway
mvn clean package -DskipTests

# Restart
cd ..
./stop-all.sh
./start-with-load.sh

# Wait 60 seconds
```

### **Step 2: Verify Logging Works**

```bash
# Check Gateway logs for revenue data
tail -f logs/api-gateway.log | grep APM_REVENUE_GATEWAY
```

You should see:
```
APM_REVENUE_GATEWAY: orderId=123, orderValue=179.98, itemCount=2, ...
```

### **Step 3: Configure APM Data Collectors**

Follow the steps for your APM tool above (AppDynamics, Datadog, etc.)

### **Step 4: Generate Test Orders**

```bash
# Manual test
curl -X POST http://your-server:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: test-123" \
  -d '{"userId":"testuser","items":[{"productId":1,"productName":"Test","price":100,"quantity":1}]}'

# Or let load generator run
# It will automatically create orders
```

### **Step 5: Check APM (Wait 5-10 minutes)**

```
APM → Services → api-gateway
→ Business Transactions → POST /api/orders/checkout
→ Click on recent transaction
→ Look for Custom Data / Data Collectors section
→ Should see: order_value, order_id, item_count
```

---

## 🎯 Expected Results

**In APM Transaction Details:**
```
Transaction: POST /api/orders/checkout
Duration: 125ms
Status: 200

Custom Data / Data Collectors:
├─ order_id: 123
├─ order_value: 179.98
├─ item_count: 2
├─ user_id: user95
├─ session_id: loadgen-session-...
└─ journey_id: loadgen-journey-...
```

**In APM Analytics:**
```sql
SELECT 
  SUM(order_value) as total_revenue,
  COUNT(*) as total_orders,
  AVG(order_value) as avg_order_value
FROM transactions
WHERE service = 'api-gateway'
  AND endpoint = 'POST /api/orders/checkout'
  AND order_value IS NOT NULL
```

**Results:**
```
total_revenue: $4,499.50
total_orders: 25
avg_order_value: $179.98
```

---

## ✅ Advantages of This Approach

| Aspect | Benefit |
|--------|---------|
| **No Multi-Service Setup** | ✅ Works with Gateway-only APM |
| **No Header Dependency** | ✅ Parses response body directly |
| **Reliable** | ✅ Gets data from actual Order response |
| **Flexible** | ✅ Can extract any JSON field |
| **Backward Compatible** | ✅ Still includes headers for debugging |

---

## 🔧 Troubleshooting

### **Issue: Local variables show as null**

**Cause:** APM captured variables before they were assigned

**Fix:** Make sure APM captures at method EXIT, not entry

**AppDynamics:**
```
Capture Point: Method Exit
or
Capture Point: After all statements
```

### **Issue: Can't see data collectors in APM**

**Check:**
1. Wait 5-10 minutes after configuration
2. Generate NEW transactions (old ones won't have data)
3. Check APM agent logs for instrumentation errors
4. Verify class/method names are exact (case-sensitive)

### **Issue: JSON parsing fails**

**Check Gateway logs:**
```bash
tail -f logs/api-gateway.log | grep "Failed to parse order response"
```

If you see errors, the Order Service might be returning unexpected format.

---

## 📋 Summary

**What We Did:**
1. ✅ Added JSON parsing to Gateway's `checkout()` method
2. ✅ Extracted revenue data into local variables
3. ✅ Added structured logging for fallback
4. ✅ Created APM configuration guide

**What You Need To Do:**
1. Deploy code changes (git pull, rebuild, restart)
2. Configure APM data collectors for your specific tool
3. Wait 5-10 minutes
4. Check APM for revenue data

**Result:** Revenue data captured from Gateway without needing Order Service instrumentation! 🎉

