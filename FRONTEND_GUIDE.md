# Frontend Web UI Guide

## Overview

The E-Commerce Demo now includes a modern, user-friendly web interface that allows you to interact with the application through your browser!

## 🚀 Quick Start

### Start with Web UI

```bash
./start-with-ui.sh
```

Wait ~40 seconds for all services to start, then open your browser:

**http://localhost:3000**

### Start with UI + Load Generator

```bash
./start-with-load.sh
```

This starts the UI plus the load generator for APM monitoring.

## 🌐 Web UI Features

### 1. Product Browsing
- 📦 View all available products
- 💰 See prices and stock levels
- 🎯 Add products to cart with custom quantities

### 2. Shopping Cart
- 🛒 View all items in your cart
- 📊 See cart total and item count
- ❌ Remove individual items
- 🗑️ Clear entire cart
- ✅ Proceed to checkout

### 3. Order Management
- 📋 View all your orders
- 📅 See order dates and status
- 💵 View order totals and details
- ✅ Track order confirmation

### 4. User Management
- 👤 Switch between different user IDs
- 💾 Load user-specific cart and orders
- 🔄 Seamless user switching

## 🎨 UI Components

### Navigation Bar
- **Products** - Browse the catalog
- **Cart** - View shopping cart (shows item count badge)
- **Orders** - View order history

### User Section
- Enter any user ID (default: `user123`)
- Click "Load My Data" to refresh

### Product Cards
- Product icon
- Name and description
- Price and stock information
- Quantity selector
- "Add to Cart" button

### Shopping Cart
- List of cart items with icons
- Quantity and price for each item
- Total items and amount
- Checkout and Clear Cart buttons

### Orders List
- Order ID and date
- Order status (Pending, Confirmed, etc.)
- List of items in each order
- Order totals

## 📱 Responsive Design

The UI is fully responsive and works on:
- 💻 Desktop computers
- 📱 Tablets
- 📱 Mobile phones

## 🎯 Usage Examples

### Example 1: Complete Purchase Flow

1. Open http://localhost:3000
2. Browse products on the Products page
3. Select quantity and click "Add to Cart" on items you want
4. Click the "Cart" button in navigation
5. Review your items
6. Click "Proceed to Checkout"
7. See order confirmation
8. Click "View Orders" to see your new order

### Example 2: Multi-User Shopping

```bash
# User 1 shopping
1. Enter "customer1" in user ID field
2. Add items to cart
3. Checkout

# User 2 shopping
1. Enter "customer2" in user ID field
2. Add different items to cart
3. Checkout

# View User 1's orders
1. Enter "customer1"
2. Click "Load My Data"
3. Go to Orders page
```

### Example 3: Cart Management

1. Add multiple products to cart
2. Go to Cart page
3. Remove unwanted items
4. Update quantities (add same product again)
5. Clear cart if needed
6. Start fresh

## 🎨 Visual Features

### Modern Design
- Clean, professional interface
- Card-based layouts
- Smooth animations
- Responsive grid layouts

### Color Scheme
- **Primary**: Indigo (buttons, prices)
- **Success**: Green (success messages)
- **Danger**: Red (errors, remove buttons)
- **Neutral**: Gray (text, borders)

### Icons
- 💻 Laptop
- 🖱️ Mouse
- ⌨️ Keyboard
- 🖥️ Monitor
- 🎧 Headphones
- 📦 Default product

### Notifications
- ✅ Success toasts (green)
- ❌ Error toasts (red)
- Auto-dismiss after 3 seconds
- Slide-in animation

## 🔧 Technical Details

### Architecture

```
Browser (Port 3000)
    ↓
Frontend Service (Spring Boot)
    ↓ (serves static files)
HTML/CSS/JavaScript
    ↓ (API calls)
API Gateway (Port 8080)
    ↓
Backend Microservices
```

### Technology Stack

- **Backend**: Spring Boot (serves static files)
- **Frontend**: Vanilla JavaScript (no frameworks)
- **Styling**: Custom CSS with CSS Variables
- **HTTP**: Fetch API for REST calls
- **Storage**: None (stateless, all data in backend)

### API Integration

The UI communicates with the API Gateway:

```javascript
// Base URL
http://localhost:8080/api

// Endpoints used
GET  /api/products           - List products
GET  /api/cart/{userId}      - Get user's cart
POST /api/cart/{userId}/items - Add to cart
DELETE /api/cart/{userId}/items/{itemId} - Remove item
DELETE /api/cart/{userId}    - Clear cart
POST /api/orders/checkout    - Create order
GET  /api/orders/user/{userId} - Get user orders
```

## 🎬 Demo Scenarios

### Scenario 1: Quick Demo (2 minutes)

```bash
# 1. Start services
./start-with-ui.sh

# 2. Open browser
open http://localhost:3000

# 3. Show features
- Browse products
- Add 2-3 items to cart
- View cart
- Checkout
- View orders
```

### Scenario 2: Multi-User Demo (5 minutes)

```bash
# Show cart isolation between users
User: user1 → Add items → Checkout
User: user2 → Add different items → Checkout
User: user1 → View orders (only their orders)
```

### Scenario 3: Full Feature Tour (10 minutes)

```bash
1. Product browsing
   - Show all products
   - Explain stock levels
   - Show quantity selection

2. Cart operations
   - Add multiple items
   - Show cart badge update
   - Remove items
   - Clear cart

3. Checkout process
   - Review cart
   - Complete checkout
   - Show confirmation modal

4. Order history
   - View all orders
   - Show order details
   - Explain order status
```

## 🐛 Troubleshooting

### UI Not Loading

```bash
# Check if frontend service is running
curl http://localhost:3000

# Check logs
tail -f logs/frontend.log
```

### API Errors

```bash
# Check if API Gateway is running
curl http://localhost:8080/api/health

# Check if backend services are running
curl http://localhost:8081/api/products
```

### CORS Issues

The frontend is configured to call `localhost:8080`. If you're accessing from a different host, update:

```javascript
// In app.js
const API_BASE_URL = 'http://YOUR_HOST:8080/api';
```

### Cache Issues

Force reload in browser:
- Chrome/Edge: `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
- Firefox: `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac)

## 📊 With Load Generator

When running with load generator (`start-with-load.sh`):

1. Open UI: http://localhost:3000
2. Open Load Stats: http://localhost:9090/stats
3. Use UI normally (your actions are separate from generated load)
4. Show APM tool with both generated load and manual actions

## 🎨 Customization

### Change Colors

Edit `frontend/src/main/resources/static/css/styles.css`:

```css
:root {
    --primary-color: #4F46E5;  /* Change to your color */
    --secondary-color: #10B981;
    --danger-color: #EF4444;
}
```

### Add Features

Edit `frontend/src/main/resources/static/js/app.js`:

```javascript
// Add new functionality
async function myNewFeature() {
    // Your code here
}
```

### Change Port

Edit `frontend/src/main/resources/application.properties`:

```properties
server.port=3000  # Change to your port
```

## 🚀 Deployment

### Local Development

```bash
cd frontend
mvn spring-boot:run
```

### Production Build

```bash
cd frontend
mvn clean package
java -jar target/frontend-1.0.0.jar
```

### Docker (Future)

```dockerfile
FROM openjdk:17-slim
COPY target/frontend-1.0.0.jar app.jar
EXPOSE 3000
CMD ["java", "-jar", "app.jar"]
```

## 📚 Files Structure

```
frontend/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/demo/frontend/
│       │       └── FrontendApplication.java
│       └── resources/
│           ├── application.properties
│           └── static/
│               ├── index.html       # Main HTML
│               ├── css/
│               │   └── styles.css   # All styles
│               └── js/
│                   └── app.js       # All JavaScript
```

## 🎯 Best Practices

1. **Always start backend services first** (they start automatically with scripts)
2. **Wait for all services to initialize** (~40 seconds)
3. **Use consistent user IDs** for testing
4. **Clear cart between demos** for clean starts
5. **Check browser console** for any errors

## 🆘 Support

If you encounter issues:

1. Check all services are running (5 services + frontend = 6 total)
2. Check logs in `logs/` directory
3. Verify ports are not in use (3000, 8080-8083, 9090)
4. Restart services if needed

## 🎉 Features to Show

When demonstrating:

✅ Beautiful, modern UI
✅ Real-time cart updates
✅ Smooth animations
✅ Toast notifications
✅ Order confirmations
✅ Multi-user support
✅ Responsive design
✅ Complete e-commerce flow

---

**Enjoy your new Web UI!** 🎉

Open http://localhost:3000 and start shopping! 🛒


