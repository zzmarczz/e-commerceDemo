# APM Revenue Tracking Configuration Guide

## ✅ Headers Are Working!

Test confirms revenue headers are in HTTP response:
```http
HTTP/1.1 200 OK
OrderId: 345
OrderValue: 999.99
ItemCount: 1
```

**Test Command:**
```bash
curl -i http://your-server:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{"userId":"test","items":[{"productId":1,"productName":"Laptop","price":999.99,"quantity":1}]}'
```

---

## 🔧 APM Configuration Required

Most APM tools **do NOT capture custom headers by default**. You must configure them explicitly.

---

## AppDynamics Configuration

### Method 1: Data Collectors (Recommended)

1. **Applications** → **[Your App]** → **Configuration** → **Instrumentation**
2. **Data Collectors** → **Add**
3. Configure:
   ```
   Name: Revenue Headers
   Type: HTTP Response Header
   
   Headers to collect:
   - OrderId
   - OrderValue
   - ItemCount
   ```

### Method 2: Business Transaction Settings

1. **Business Transactions** → Find `POST /api/orders/checkout`
2. **Transaction Snapshots** → **Data Collectors**
3. Enable **Collect All HTTP Headers** or specify:
   - `OrderId`
   - `OrderValue`
   - `ItemCount`

### Method 3: Analytics Schema

1. **Analytics** → **Configuration** → **Transaction Analytics**
2. Add custom fields:
   ```
   Field Name: order_value
   Source: HTTP Response Header
   Header Name: OrderValue
   Data Type: Double
   
   Field Name: order_id
   Source: HTTP Response Header
   Header Name: OrderId
   Data Type: String
   
   Field Name: item_count
   Source: HTTP Response Header
   Header Name: ItemCount
   Data Type: Integer
   ```

3. Wait 5-10 minutes for schema changes to apply

### Verification in AppDynamics

After configuration:

1. **Application Dashboard** → **Business Transactions** → `POST /api/orders/checkout`
2. Click on any recent transaction
3. **Transaction Snapshot** → **HTTP Response Headers**
4. You should see: `OrderId`, `OrderValue`, `ItemCount`

5. For analytics:
   ```sql
   SELECT 
     segments.userData.OrderValue as order_value,
     segments.userData.ItemCount as item_count,
     segments.userData.OrderId as order_id
   FROM transactions
   WHERE segments.businessTransaction.name = 'POST /api/orders/checkout'
   ```

---

## Datadog Configuration

### Java Agent Configuration

Edit `datadog.yaml` or set environment variables:

```yaml
apm_config:
  # Capture specific headers
  header_tags:
    - header: OrderId
      tag_name: http.response.order_id
    - header: OrderValue
      tag_name: http.response.order_value
    - header: ItemCount
      tag_name: http.response.item_count
```

### Environment Variables

```bash
export DD_TRACE_HEADER_TAGS="OrderId:order_id,OrderValue:order_value,ItemCount:item_count"
export DD_TRACE_RESPONSE_HEADER_TAGS="OrderId,OrderValue,ItemCount"
```

### Restart Services

```bash
./stop-all.sh
./start-with-load.sh
```

### Query in Datadog

```
# APM Analytics
service:api-gateway resource_name:"POST /api/orders/checkout" @http.response.order_value:*

# Aggregate revenue
SUM(@http.response.order_value) BY @http.response.order_id
```

---

## New Relic Configuration

### Java Agent Config (`newrelic.yml`)

```yaml
common: &default_settings
  attributes:
    enabled: true
    include:
      - response.headers.OrderId
      - response.headers.OrderValue
      - response.headers.ItemCount

  transaction_tracer:
    attributes:
      enabled: true
      include:
        - response.headers.*

  transaction_events:
    attributes:
      enabled: true
      include:
        - response.headers.OrderId
        - response.headers.OrderValue
        - response.headers.ItemCount
```

### Query (NRQL)

```sql
SELECT 
  sum(numeric(response.headers.OrderValue)) as 'Total Revenue',
  count(*) as 'Total Orders',
  average(numeric(response.headers.OrderValue)) as 'Avg Order Value'
FROM Transaction
WHERE request.uri = '/api/orders/checkout'
AND httpResponseCode = '200'
SINCE 1 hour ago
```

---

## Elastic APM Configuration

### Java Agent Properties

```properties
# elasticapm.properties
elastic.apm.capture_headers=true
elastic.apm.capture_body=all
elastic.apm.sanitize_field_names=
```

### Or Environment Variables

```bash
export ELASTIC_APM_CAPTURE_HEADERS=true
export ELASTIC_APM_CAPTURE_BODY=all
```

### Query in Kibana

```json
GET apm-*/_search
{
  "query": {
    "bool": {
      "must": [
        { "term": { "transaction.type": "request" }},
        { "term": { "http.request.method": "POST" }},
        { "term": { "url.path": "/api/orders/checkout" }}
      ]
    }
  },
  "aggs": {
    "total_revenue": {
      "sum": { "field": "http.response.headers.OrderValue" }
    }
  }
}
```

---

## Dynatrace Configuration

### Create Custom Request Attributes

1. **Settings** → **Server-side service monitoring** → **Request attributes**
2. **Define a new request attribute**

**For OrderValue:**
```
Request attribute name: Revenue
Data type: Double
Data source: Response → HTTP response header
Parameter name: OrderValue
```

**For ItemCount:**
```
Request attribute name: Items
Data type: Integer
Data source: Response → HTTP response header
Parameter name: ItemCount
```

**For OrderId:**
```
Request attribute name: OrderID
Data type: Text
Data source: Response → HTTP response header
Parameter name: OrderId
```

3. **Capture rule** → Service: api-gateway, Path: `/api/orders/checkout`

### Query in Dynatrace

```
// Using DQL (Dynatrace Query Language)
fetch dt.entity.service, 
      from:now()-1h
| filter serviceName == "api-gateway"
| fields Revenue = customAttributes.Revenue,
         Items = customAttributes.Items,
         OrderID = customAttributes.OrderID
| summarize TotalRevenue = sum(Revenue),
            TotalOrders = count(),
            AvgOrderValue = avg(Revenue)
```

---

## Generic Solution: Response Body Parsing

If your APM can't capture headers, revenue data is **already in the response body**:

```json
{
  "id": 345,
  "userId": "testuser123",
  "totalAmount": 999.99,    ← Revenue here!
  "items": [
    {
      "quantity": 1           ← Item count here!
    }
  ]
}
```

Most APM tools can parse JSON response bodies automatically.

---

## Test Your APM Configuration

### 1. Generate Some Orders

```bash
# Run load generator for 5 minutes
cd ~/e-commerceDemo
./start-with-load.sh
```

Wait 5 minutes.

### 2. Check APM for These Fields

Look for transaction `POST /api/orders/checkout` and verify you can see:
- `OrderId` or `order_id` or `http.response.headers.OrderId`
- `OrderValue` or `order_value`
- `ItemCount` or `item_count`

Field names vary by APM tool!

### 3. Common Field Name Patterns

| APM Tool      | Field Name Pattern                    |
|---------------|--------------------------------------|
| AppDynamics   | `segments.userData.OrderValue`       |
| Datadog       | `@http.response.order_value`         |
| New Relic     | `response.headers.OrderValue`        |
| Elastic       | `http.response.headers.OrderValue`   |
| Dynatrace     | `customAttributes.Revenue`           |

---

## Troubleshooting

### Headers Not Showing Up?

1. **Check configuration is saved and applied**
2. **Restart APM agent/services** (required for config changes)
3. **Wait 5-10 minutes** for configuration to propagate
4. **Generate new transactions** (old transactions won't have headers)
5. **Check APM documentation** for exact field naming

### Still Not Working?

**Test with curl** to verify headers are in the response:
```bash
curl -i http://your-server:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{"userId":"test","items":[{"productId":1,"productName":"Test","price":100,"quantity":1}]}'
```

If you see `OrderId`, `OrderValue`, `ItemCount` in the curl output, the problem is **APM configuration**, not the application.

---

## Alternative: Query Metrics Endpoint

If headers remain problematic, query the metrics endpoint:

```bash
curl http://your-server:8080/api/metrics/revenue
```

Returns:
```json
{
  "totalRevenue": "4499.50",
  "totalOrders": 25,
  "averageOrderValue": "179.98",
  "timestamp": "2025-12-02T07:07:26Z"
}
```

Configure APM to poll this endpoint every 60 seconds for revenue KPIs.

---

## Summary

**Application Side: ✅ WORKING**
- Headers are in HTTP response
- Verified with curl
- Code is correct

**APM Side: ⚠️ NEEDS CONFIGURATION**
- Most APM tools don't capture custom headers by default
- Must configure explicitly
- Follow instructions for your specific APM tool

**Next Steps:**
1. Configure your APM to capture custom headers
2. Restart services if required
3. Wait 5-10 minutes
4. Generate new transactions
5. Check APM for revenue fields

