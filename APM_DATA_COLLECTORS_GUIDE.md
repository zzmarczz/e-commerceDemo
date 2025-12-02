# APM Data Collectors for Revenue Tracking

## 🎯 Method-Based Revenue Capture (Better than Headers!)

Instead of relying on HTTP headers, you can configure your APM to capture revenue data **directly from the Java method** that creates orders.

---

## 📍 Target Method for Instrumentation

**Class:** `com.demo.order.controller.OrderController`  
**Method:** `checkout`  
**Service:** `order-service` (port 8083)

**Method Signature:**
```java
public ResponseEntity<Order> checkout(
    @RequestBody CheckoutRequest request,
    @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
    @RequestHeader(value = "X-Journey-ID", required = false) String journeyId
)
```

**Local Variables to Capture:**
- `total` (Double) - Order total amount
- `itemCount` (Integer) - Number of items
- `savedOrder.getId()` (Long) - Order ID
- `userId` (String) - From request.getUserId()
- `sessionId` (String) - From method parameter

---

## 🔧 Configuration by APM Tool

---

## 1️⃣ AppDynamics Data Collectors

### **Method A: GUI Configuration (Recommended)**

**Step 1: Navigate to Data Collectors**
```
Applications 
→ [Your App] 
→ Configuration 
→ Instrumentation 
→ Data Collectors 
→ Add
```

**Step 2: Configure Method Invocation Data Collector**

**Collector 1: Order Value**
```
Name: Revenue - Order Value
Data Collector Type: Method Invocation Data Collector

Class Name: com.demo.order.controller.OrderController
Method Name: checkout
Data Gathered From: Return Value

Navigate to: getId()    [returns Order ID]
Then navigate to: getTotalAmount()    [returns revenue]

Display Name: order_value
Data Type: DOUBLE
```

**Collector 2: Order ID**
```
Name: Revenue - Order ID
Class: com.demo.order.controller.OrderController
Method: checkout
Data Gathered From: Return Value
Navigate to: getId()
Display Name: order_id
Data Type: LONG
```

**Collector 3: Item Count (from local variable)**
```
Name: Revenue - Item Count
Class: com.demo.order.controller.OrderController
Method: checkout
Data Gathered From: Local Variable
Variable Name: itemCount
Display Name: item_count
Data Type: INTEGER
```

**Collector 4: User ID (from method parameter)**
```
Name: Revenue - User ID
Class: com.demo.order.controller.OrderController
Method: checkout
Data Gathered From: Method Parameter
Parameter Index: 0    [CheckoutRequest]
Navigate to: getUserId()
Display Name: user_id
Data Type: STRING
```

**Step 3: Assign to Business Transaction**
```
Business Transactions → POST /api/orders/checkout
→ Transaction Snapshots → Data Collectors
→ Select the collectors you just created
```

**Step 4: Enable in Analytics**
```
Analytics → Configuration → Transaction Analytics
→ Add Fields:
  - order_value (Double)
  - order_id (Long)
  - item_count (Integer)
  - user_id (String)
```

---

### **Method B: Custom Match Rule (XML)**

Create a custom match rule file:

**File:** `custom-match-rules.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<custom-match-rules version="1.0">
  
  <!-- Match the checkout method -->
  <match-rule>
    <rule-name>OrderController.checkout</rule-name>
    <rule-description>Capture revenue data from checkout method</rule-description>
    
    <match-specification>
      <match-class>
        <class-name>com.demo.order.controller.OrderController</class-name>
      </match-class>
      <match-method>
        <method-name>checkout</method-name>
      </match-method>
    </match-specification>
    
    <!-- Data to collect -->
    <data-collector-list>
      
      <!-- Capture order value from return value -->
      <data-collector>
        <name>order_value</name>
        <gather-info>
          <gather-from>RETURN_VALUE</gather-from>
          <navigation>
            <operation>INVOKE_METHOD</operation>
            <operation-params>
              <operation-param>getBody</operation-param>
            </operation-params>
            <next-navigation>
              <operation>INVOKE_METHOD</operation>
              <operation-params>
                <operation-param>getTotalAmount</operation-param>
              </operation-params>
            </next-navigation>
          </navigation>
        </gather-info>
      </data-collector>
      
      <!-- Capture order ID from return value -->
      <data-collector>
        <name>order_id</name>
        <gather-info>
          <gather-from>RETURN_VALUE</gather-from>
          <navigation>
            <operation>INVOKE_METHOD</operation>
            <operation-params>
              <operation-param>getBody</operation-param>
            </operation-params>
            <next-navigation>
              <operation>INVOKE_METHOD</operation>
              <operation-params>
                <operation-param>getId</operation-param>
              </operation-params>
            </next-navigation>
          </navigation>
        </gather-info>
      </data-collector>
      
      <!-- Capture item count from local variable -->
      <data-collector>
        <name>item_count</name>
        <gather-info>
          <gather-from>LOCAL_VARIABLE</gather-from>
          <variable-name>itemCount</variable-name>
        </gather-info>
      </data-collector>
      
    </data-collector-list>
  </match-rule>
  
</custom-match-rules>
```

**Deploy:**
```bash
# Place in AppDynamics config directory
cp custom-match-rules.xml /path/to/appd-agent/ver*/conf/
# Restart application
```

---

### **Method C: Log-Based Data Collector**

Easiest method using the structured logging we added:

```
Data Collector Type: Log Message
Log Level: INFO
Logger Name: com.demo.order.controller.OrderController
Message Pattern: APM_REVENUE: orderId=(.*), orderValue=(.*), itemCount=(.*)

Extract Fields:
- Field 1: order_id (capture group 1)
- Field 2: order_value (capture group 2)
- Field 3: item_count (capture group 3)
```

---

## 2️⃣ Datadog APM - Custom Instrumentation

### **Option A: Using DD Trace Annotations**

Add Datadog dependency to `order-service/pom.xml`:

```xml
<dependency>
    <groupId>com.datadoghq</groupId>
    <artifactId>dd-trace-api</artifactId>
    <version>1.24.0</version>
</dependency>
```

Modify `OrderController.java`:

```java
import datadog.trace.api.Trace;
import datadog.trace.api.interceptor.MutableSpan;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

@PostMapping("/checkout")
@Trace(operationName = "order.checkout", resourceName = "POST /checkout")
public ResponseEntity<Order> checkout(...) {
    // ... existing code ...
    
    // After order is created
    Span span = GlobalTracer.get().activeSpan();
    if (span != null && span instanceof MutableSpan) {
        MutableSpan mutableSpan = (MutableSpan) span;
        mutableSpan.setTag("order.id", savedOrder.getId());
        mutableSpan.setTag("order.value", total);
        mutableSpan.setTag("order.item_count", itemCount);
        mutableSpan.setTag("order.user_id", userId);
    }
    
    return ResponseEntity.ok()...
}
```

**Query in Datadog:**
```
service:order-service resource_name:"POST /checkout" @order.value:*

# Aggregate revenue
SUM(@order.value) BY @order.id
```

---

### **Option B: Datadog Java Tracer Configuration**

In `dd-java-agent` configuration:

```properties
# dd.properties or environment variables
DD_TRACE_METHODS=com.demo.order.controller.OrderController[checkout]
DD_TRACE_ANNOTATIONS=org.springframework.web.bind.annotation.PostMapping
```

---

## 3️⃣ New Relic - Custom Instrumentation

### **Option A: Java Agent API**

Add dependency to `order-service/pom.xml`:

```xml
<dependency>
    <groupId>com.newrelic.agent.java</groupId>
    <artifactId>newrelic-api</artifactId>
    <version>8.7.0</version>
</dependency>
```

Modify `OrderController.java`:

```java
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

@PostMapping("/checkout")
@Trace(dispatcher = true)
public ResponseEntity<Order> checkout(...) {
    // ... existing code ...
    
    // After order is created
    NewRelic.addCustomParameter("order_id", savedOrder.getId());
    NewRelic.addCustomParameter("order_value", total);
    NewRelic.addCustomParameter("item_count", itemCount);
    NewRelic.addCustomParameter("user_id", userId);
    
    return ResponseEntity.ok()...
}
```

**Query (NRQL):**
```sql
SELECT 
  sum(numeric(order_value)) as 'Total Revenue',
  count(*) as 'Total Orders',
  average(numeric(order_value)) as 'Avg Order Value'
FROM Transaction
WHERE appName = 'order-service'
  AND name = 'WebTransaction/SpringController/OrderController/checkout'
SINCE 1 hour ago
```

---

### **Option B: Custom Instrumentation XML**

Create `extensions/OrderControllerExtension.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<extension xmlns="https://newrelic.com/docs/java/xsd/v1.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="newrelic-extension extension.xsd"
          name="order-controller-instrumentation"
          version="1.0">
  
  <instrumentation>
    <pointcut transactionStartPoint="false">
      <className>com.demo.order.controller.OrderController</className>
      <method>
        <name>checkout</name>
        <parameters>
          <parameter>com.demo.order.dto.CheckoutRequest</parameter>
          <parameter>java.lang.String</parameter>
          <parameter>java.lang.String</parameter>
        </parameters>
      </method>
      
      <!-- Capture return value attributes -->
      <captureReturn>
        <captureParameter name="order_id">
          <navigation>
            <method>getBody</method>
            <method>getId</method>
          </navigation>
        </captureParameter>
        <captureParameter name="order_value">
          <navigation>
            <method>getBody</method>
            <method>getTotalAmount</method>
          </navigation>
        </captureParameter>
      </captureReturn>
    </pointcut>
  </instrumentation>
  
</extension>
```

---

## 4️⃣ Elastic APM - Custom Instrumentation

### **Option A: Elastic APM API**

Add dependency:

```xml
<dependency>
    <groupId>co.elastic.apm</groupId>
    <artifactId>apm-agent-api</artifactId>
    <version>1.42.0</version>
</dependency>
```

Modify `OrderController.java`:

```java
import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;

@PostMapping("/checkout")
public ResponseEntity<Order> checkout(...) {
    Transaction transaction = ElasticApm.currentTransaction();
    
    // ... existing code ...
    
    // After order is created
    transaction.setLabel("order_id", savedOrder.getId().toString());
    transaction.setLabel("order_value", total);
    transaction.setLabel("item_count", itemCount);
    transaction.setLabel("user_id", userId);
    
    return ResponseEntity.ok()...
}
```

**Query in Kibana:**
```json
GET apm-*/_search
{
  "query": {
    "bool": {
      "must": [
        { "term": { "processor.event": "transaction" }},
        { "term": { "transaction.name": "OrderController#checkout" }}
      ]
    }
  },
  "aggs": {
    "total_revenue": {
      "sum": { "field": "labels.order_value" }
    },
    "avg_order_value": {
      "avg": { "field": "labels.order_value" }
    }
  }
}
```

---

## 5️⃣ Dynatrace - Custom Service Methods

### **Dynatrace Automatic Instrumentation**

Dynatrace automatically instruments Spring controllers. To capture custom attributes:

**Step 1: Enable Method Argument Capture**

```
Settings 
→ Server-side service monitoring 
→ Deep monitoring 
→ Custom services
→ Add custom service
```

**Configuration:**
```
Service class: com.demo.order.controller.OrderController
Service method: checkout
Capture:
  - Return value → getBody() → getTotalAmount() as "revenue"
  - Return value → getBody() → getId() as "order_id"
  - Parameter[0] → getUserId() as "user_id"
```

---

### **Option B: Dynatrace OneAgent SDK**

Add dependency:

```xml
<dependency>
    <groupId>com.dynatrace.oneagent.sdk.java</groupId>
    <artifactId>oneagent-sdk</artifactId>
    <version>1.8.0</version>
</dependency>
```

Modify code:

```java
import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

private static final OneAgentSDK oneAgentSdk = OneAgentSDKFactory.createInstance();

@PostMapping("/checkout")
public ResponseEntity<Order> checkout(...) {
    // ... existing code ...
    
    // After order is created
    oneAgentSdk.addCustomRequestAttribute("order_value", String.valueOf(total));
    oneAgentSdk.addCustomRequestAttribute("order_id", savedOrder.getId().toString());
    oneAgentSdk.addCustomRequestAttribute("item_count", String.valueOf(itemCount));
    
    return ResponseEntity.ok()...
}
```

---

## 📊 Comparison: Headers vs Method Instrumentation

| Aspect | HTTP Headers | Method Instrumentation |
|--------|--------------|------------------------|
| **Reliability** | ❌ Can be dropped by Gateway | ✅ Captured at source |
| **Setup Complexity** | ✅ Simple (just add headers) | ⚠️ Requires APM config |
| **Data Richness** | ⚠️ Limited to header values | ✅ Can capture any variable |
| **Performance** | ✅ Minimal overhead | ⚠️ Slight overhead |
| **Gateway Dependency** | ❌ Must forward headers | ✅ Direct capture |
| **Visibility** | ✅ Visible in browser/curl | ❌ APM only |
| **Best For** | Quick setup, debugging | Production, detailed analytics |

---

## 🎯 **Recommended Approach: Use BOTH**

**For Maximum Reliability:**

1. ✅ **Headers** - For quick debugging and browser visibility
2. ✅ **Method Instrumentation** - For reliable APM analytics

**Why both:**
- Headers work immediately without APM configuration
- Method instrumentation survives Gateway issues
- Redundancy ensures revenue data is always captured

---

## ✅ **Quick Start for Your APM**

**Which APM tool are you using?**

1. **AppDynamics** → Use Method A: GUI Configuration (easiest)
2. **Datadog** → Use Option A: DD Trace Annotations
3. **New Relic** → Use Option A: Java Agent API
4. **Elastic APM** → Use Option A: Elastic APM API
5. **Dynatrace** → Use Step 1: Enable Method Argument Capture

**All methods above capture data from:**
```
Class: com.demo.order.controller.OrderController
Method: checkout
Variables: total, itemCount, savedOrder.getId(), userId
```

---

## 🔍 Verify It's Working

After configuration:

1. **Trigger a checkout** (manually or via load generator)
2. **Wait 2-5 minutes** for APM to collect data
3. **Find the transaction** in APM
4. **Look for custom attributes/tags:**
   - `order_value` or `order.value` or `revenue`
   - `order_id`
   - `item_count`
5. **Create revenue dashboard** using these fields

---

## 📋 Next Steps

1. Choose your APM tool from the list above
2. Follow the configuration steps
3. Rebuild and restart `order-service` if you added API dependencies
4. Generate test orders
5. Verify data appears in APM
6. Create revenue analytics dashboards

**Method instrumentation is MORE RELIABLE than headers for production APM!** ✅

