# Custom IP Address Setup Guide

If you're accessing the application from a different IP address (not localhost), you need to configure CORS and API URLs.

## Quick Setup (Automated)

### Step 1: Run Configuration Script

```bash
./configure-ip.sh YOUR_IP_ADDRESS
```

**Example:**
```bash
./configure-ip.sh 192.168.1.100
```

This automatically updates:
- CORS configuration in API Gateway
- Frontend API URL
- Backend service URLs

### Step 2: Rebuild

```bash
mvn clean install -DskipTests
```

### Step 3: Restart Services

```bash
./stop-all.sh
./start-with-ui.sh
```

### Step 4: Access Application

Open your browser to:
```
http://YOUR_IP_ADDRESS:3000
```

---

## Manual Setup (If Script Doesn't Work)

### 1. Update CORS Configuration

**File:** `api-gateway/src/main/java/com/demo/gateway/config/CorsConfig.java`

Find this line:
```java
config.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://127.0.0.1:3000"
));
```

Add your IP:
```java
config.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://YOUR_IP:3000"  // ← Add this line
));
```

### 2. Update Frontend API URL

**File:** `frontend/src/main/resources/static/js/app.js`

Change line 4:
```javascript
// From:
const API_BASE_URL = 'http://localhost:8080/api';

// To:
const API_BASE_URL = 'http://YOUR_IP:8080/api';
```

### 3. Update API Gateway Service URLs

**File:** `api-gateway/src/main/resources/application.properties`

Change:
```properties
# From:
services.product.url=http://localhost:8081
services.cart.url=http://localhost:8082
services.order.url=http://localhost:8083

# To:
services.product.url=http://YOUR_IP:8081
services.cart.url=http://YOUR_IP:8082
services.order.url=http://YOUR_IP:8083
```

### 4. Rebuild and Restart

```bash
mvn clean install -DskipTests
./stop-all.sh
./start-with-ui.sh
```

---

## Common Scenarios

### Scenario 1: Local Network Access

**Situation:** Accessing from another computer on the same network

**Your IP:** Find with `ifconfig` or `ipconfig`
- macOS/Linux: `ifconfig | grep inet`
- Windows: `ipconfig`

**Example:** 192.168.1.100

**Configuration:**
```bash
./configure-ip.sh 192.168.1.100
```

**Access from other computers:**
```
http://192.168.1.100:3000
```

### Scenario 2: Remote Server

**Situation:** Running on a cloud server or VPS

**Your IP:** Server's public IP address

**Example:** 203.0.113.50

**Configuration:**
```bash
./configure-ip.sh 203.0.113.50
```

**Access:**
```
http://203.0.113.50:3000
```

**Important:** Make sure ports 3000, 8080-8083 are open in your firewall!

### Scenario 3: Multiple IPs

**Situation:** Need to allow access from multiple IPs

**Edit CORS manually:**

```java
config.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://192.168.1.100:3000",
    "http://192.168.1.101:3000",
    "http://10.0.0.50:3000"
));
```

Or allow all (⚠️ not recommended for production):
```java
config.addAllowedOrigin("*");
```

---

## Firewall Configuration

### macOS

Allow incoming connections:
```bash
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add /usr/bin/java
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --unblockapp /usr/bin/java
```

### Linux (UFW)

```bash
sudo ufw allow 3000
sudo ufw allow 8080
sudo ufw allow 8081
sudo ufw allow 8082
sudo ufw allow 8083
```

### Linux (firewalld)

```bash
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --permanent --add-port=8080-8083/tcp
sudo firewall-cmd --reload
```

### Windows

Open Windows Defender Firewall:
1. Go to Advanced Settings
2. Inbound Rules → New Rule
3. Port → TCP → Specific ports: 3000,8080,8081,8082,8083
4. Allow the connection
5. Apply to all profiles

---

## Cloud Provider Specific

### AWS EC2

1. Edit Security Group
2. Add Inbound Rules:
   - Type: Custom TCP
   - Port Range: 3000, 8080-8083
   - Source: 0.0.0.0/0 (or specific IPs)

### Google Cloud

```bash
gcloud compute firewall-rules create allow-ecommerce \
  --allow tcp:3000,tcp:8080-8083 \
  --source-ranges 0.0.0.0/0
```

### Azure

```bash
az network nsg rule create \
  --resource-group myResourceGroup \
  --nsg-name myNetworkSecurityGroup \
  --name allow-ecommerce \
  --priority 100 \
  --destination-port-ranges 3000 8080-8083 \
  --access Allow
```

---

## Verification

### Check Configuration

```bash
# Check CORS config
cat api-gateway/src/main/java/com/demo/gateway/config/CorsConfig.java | grep "setAllowedOrigins"

# Check Frontend API URL
cat frontend/src/main/resources/static/js/app.js | grep "API_BASE_URL"

# Check Gateway service URLs
cat api-gateway/src/main/resources/application.properties | grep "services\."
```

### Test Connectivity

```bash
# From the server
curl http://YOUR_IP:8080/api/health

# From another machine
curl http://YOUR_IP:3000
curl http://YOUR_IP:8080/api/health
```

### Browser Test

1. Open: `http://YOUR_IP:3000`
2. Press F12 (Developer Tools)
3. Go to Console tab
4. Should see no CORS errors
5. Products should load

---

## Troubleshooting

### "Network Error" Still Appears

**Check 1:** CORS configuration
```bash
curl -I -X OPTIONS http://YOUR_IP:8080/api/products \
  -H "Origin: http://YOUR_IP:3000" \
  -H "Access-Control-Request-Method: GET"

# Should show: Access-Control-Allow-Origin: http://YOUR_IP:3000
```

**Check 2:** Services running
```bash
./check-services.sh
```

**Check 3:** Firewall
```bash
# Test if port is accessible
telnet YOUR_IP 8080
```

### Services Start But Not Accessible

**Solution:** Check binding address

Edit each service's `application.properties`:
```properties
# Add this to bind to all interfaces
server.address=0.0.0.0
```

Files to edit:
- `product-service/src/main/resources/application.properties`
- `cart-service/src/main/resources/application.properties`
- `order-service/src/main/resources/application.properties`
- `api-gateway/src/main/resources/application.properties`
- `frontend/src/main/resources/application.properties`

### Connection Refused

**Possible causes:**
1. Services not running → Run `./check-services.sh`
2. Firewall blocking → Check firewall rules
3. Wrong IP → Verify IP with `ifconfig` or `ipconfig`
4. Services binding to localhost only → Add `server.address=0.0.0.0`

---

## Complete Example

### Example: Setting up on 192.168.1.100

```bash
# 1. Get your IP
ifconfig | grep "inet " | grep -v 127.0.0.1

# Output: inet 192.168.1.100

# 2. Configure
./configure-ip.sh 192.168.1.100

# 3. Rebuild
mvn clean install -DskipTests

# 4. Stop existing
./stop-all.sh

# 5. Start services
./start-with-ui.sh

# 6. Wait 60 seconds
sleep 60

# 7. Test
curl http://192.168.1.100:8080/api/health

# 8. Open browser
open http://192.168.1.100:3000
```

---

## Reverting to Localhost

To switch back to localhost:

```bash
./configure-ip.sh localhost
mvn clean install -DskipTests
./stop-all.sh
./start-with-ui.sh
```

---

## Security Considerations

⚠️ **Important Security Notes:**

1. **Don't expose to internet without authentication**
2. **Use HTTPS in production** (requires SSL certificates)
3. **Restrict CORS origins** to known IPs/domains
4. **Use firewall rules** to limit access
5. **Change default ports** if needed
6. **Add authentication** for production use

---

## Need Help?

1. Run: `./check-services.sh`
2. Check: `tail -f logs/*.log`
3. Verify: Browser console (F12) for errors
4. Test: `curl http://YOUR_IP:8080/api/health`

**Most common issue:** Firewall blocking ports or services binding to localhost only.

**Quick fix:** Add `server.address=0.0.0.0` to all service properties files.


