# Troubleshooting Guide

## "Failed to load products: Network Error"

This error typically means one of two things:

### 1. Services Not Running

**Solution:** Start the services

```bash
# If services aren't running, start them:
./start-with-ui.sh

# Wait 40-60 seconds for all services to start
```

**Verify services are running:**

```bash
# Check service health
./check-services.sh

# Or manually check each service:
curl http://localhost:3000          # Frontend
curl http://localhost:8080/api/health  # API Gateway
curl http://localhost:8081/api/products # Product Service
```

### 2. CORS Configuration Issue

**Problem:** Browser blocks frontend (port 3000) from accessing API (port 8080)

**Solution:** The CORS configuration has been added. You need to rebuild and restart:

```bash
# Stop all services
./stop-all.sh

# Start with UI (this will rebuild)
./start-with-ui.sh
```

## Complete Fix Steps

### Step 1: Stop Everything

```bash
./stop-all.sh
```

### Step 2: Rebuild (includes CORS fix)

```bash
mvn clean install -DskipTests
```

### Step 3: Start Services

```bash
./start-with-ui.sh
```

### Step 4: Wait and Verify

Wait 40-60 seconds, then check:

```bash
./check-services.sh
```

### Step 5: Test in Browser

1. Open: http://localhost:3000
2. Open browser console (F12)
3. Refresh the page
4. Products should now load

## Other Common Issues

### Port Already in Use

**Error:** "Address already in use"

**Solution:**

```bash
# Stop all services
./stop-all.sh

# If that doesn't work, manually kill processes
lsof -ti:3000 | xargs kill -9
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
lsof -ti:8082 | xargs kill -9
lsof -ti:8083 | xargs kill -9
lsof -ti:9090 | xargs kill -9

# Then start again
./start-with-ui.sh
```

### Services Start But Still Get Errors

**Check logs:**

```bash
# View logs for each service
tail -f logs/frontend.log
tail -f logs/api-gateway.log
tail -f logs/product-service.log
```

**Look for errors like:**
- "Port already in use"
- "Connection refused"
- "Unable to start"

### Browser Cache Issues

**Solution:**

1. Hard refresh: `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
2. Clear browser cache
3. Try incognito/private window

### API Gateway Not Responding

**Check if running:**

```bash
curl http://localhost:8080/api/health
```

**Expected response:** `"API Gateway is running"`

**If not running:**

```bash
# Check the log
tail -f logs/api-gateway.log

# Look for errors and restart
./stop-all.sh
./start-with-ui.sh
```

## Detailed Diagnostics

### Check Service Status

```bash
# Run the health check script
./check-services.sh
```

### Test API Directly

```bash
# Test API Gateway
curl http://localhost:8080/api/products

# Test Product Service directly
curl http://localhost:8081/api/products

# Should return JSON with products
```

### Test CORS

```bash
# Test CORS headers
curl -I -X OPTIONS http://localhost:8080/api/products \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET"

# Look for: Access-Control-Allow-Origin: http://localhost:3000
```

### Check Browser Console

1. Open browser (Chrome/Firefox)
2. Press F12 to open Developer Tools
3. Go to Console tab
4. Look for errors (red text)

**Common errors:**
- `CORS policy` - CORS not configured (rebuild needed)
- `ERR_CONNECTION_REFUSED` - Service not running
- `404 Not Found` - Wrong URL or service not ready
- `Network Error` - Service not accessible

## Service Startup Order

Services start in this order:
1. Product Service (8081) - 5 seconds
2. Cart Service (8082) - 5 seconds
3. Order Service (8083) - 5 seconds
4. API Gateway (8080) - 5 seconds
5. Frontend (3000) - 5 seconds

**Total:** ~40-50 seconds for everything to be ready

## Quick Diagnostic Commands

```bash
# Are services running?
curl http://localhost:3000
curl http://localhost:8080/api/health

# Check logs
ls -la logs/

# View all logs at once
tail -f logs/*.log

# Check if ports are in use
lsof -i :3000
lsof -i :8080
lsof -i :8081
lsof -i :8082
lsof -i :8083
```

## Still Having Issues?

### 1. Complete Reset

```bash
# Stop everything
./stop-all.sh

# Clean build
mvn clean

# Full rebuild
mvn install -DskipTests

# Start fresh
./start-with-ui.sh

# Wait 60 seconds
sleep 60

# Check status
./check-services.sh

# Open browser
open http://localhost:3000
```

### 2. Check System Requirements

```bash
# Java version (need 17+)
java -version

# Maven version (need 3.6+)
mvn -version

# Available memory
# Should have at least 2GB free RAM
```

### 3. Manual Service Start

If scripts don't work, start manually:

```bash
# Terminal 1
cd product-service && mvn spring-boot:run

# Terminal 2
cd cart-service && mvn spring-boot:run

# Terminal 3
cd order-service && mvn spring-boot:run

# Terminal 4
cd api-gateway && mvn spring-boot:run

# Terminal 5
cd frontend && mvn spring-boot:run
```

## Network Error Specific Fixes

### Fix 1: CORS Configuration (Already Applied)

The API Gateway now includes CORS configuration to allow requests from the frontend.

**File:** `api-gateway/src/main/java/com/demo/gateway/config/CorsConfig.java`

This allows:
- Origin: http://localhost:3000
- Methods: GET, POST, PUT, DELETE, OPTIONS
- Headers: All
- Credentials: Yes

### Fix 2: Frontend API URL

**File:** `frontend/src/main/resources/static/js/app.js`

Check the API_BASE_URL is correct:

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

Should NOT have trailing slash.

### Fix 3: Service Discovery

Make sure API Gateway can reach backend services.

**File:** `api-gateway/src/main/resources/application.properties`

```properties
services.product.url=http://localhost:8081
services.cart.url=http://localhost:8082
services.order.url=http://localhost:8083
```

## Prevention Tips

1. **Always wait 40-60 seconds** after starting services
2. **Check health** before opening browser: `./check-services.sh`
3. **Use scripts** instead of manual starts
4. **Check logs** if something seems wrong
5. **Stop properly** using `./stop-all.sh`

## Success Checklist

✅ All services running: `./check-services.sh` shows all green  
✅ API accessible: `curl http://localhost:8080/api/health` works  
✅ Frontend accessible: `curl http://localhost:3000` works  
✅ CORS configured: No errors in browser console  
✅ Products load: Browser shows product cards  

## Getting Help

If still stuck:

1. Run: `./check-services.sh` and share output
2. Check: `tail -f logs/*.log` for errors
3. Browser console: Press F12, share any red errors
4. Verify Java 17+ and Maven 3.6+ installed

---

**Most common fix:** Stop everything, rebuild, start fresh, wait 60 seconds.

```bash
./stop-all.sh && mvn clean install -DskipTests && ./start-with-ui.sh
```

Then wait 60 seconds and open http://localhost:3000


