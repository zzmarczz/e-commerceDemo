# AWS EC2 Deployment Guide

## Quick Fix for Your Current Error

You're getting a network error because the application isn't configured for your AWS EC2 IP address yet.

### Run This Now:

```bash
./quick-fix-ip.sh
```

Then:

```bash
mvn clean install -DskipTests
./stop-all.sh
./start-with-ui.sh
```

Wait 60 seconds, then access: `http://3.10.224.202:3000`

---

## Complete AWS EC2 Setup Guide

### Prerequisites

1. **EC2 Instance Running**
   - Ubuntu 20.04+ or Amazon Linux 2
   - t2.medium or larger recommended (2GB+ RAM)
   - Public IP address

2. **Software Installed**
   - Java 17+
   - Maven 3.6+
   - Git

### Step 1: Security Group Configuration

**CRITICAL:** Open these ports in your EC2 Security Group:

| Port | Protocol | Source | Purpose |
|------|----------|--------|---------|
| 22 | TCP | Your IP | SSH |
| 3000 | TCP | 0.0.0.0/0 | Frontend |
| 8080 | TCP | 0.0.0.0/0 | API Gateway |

**How to configure:**

1. Go to AWS Console → EC2 → Security Groups
2. Select your instance's security group
3. Click "Edit inbound rules"
4. Add rules:

```
Type: Custom TCP
Port: 3000
Source: 0.0.0.0/0 (or your IP for security)

Type: Custom TCP
Port: 8080
Source: 0.0.0.0/0 (or your IP for security)
```

### Step 2: Install Dependencies

```bash
# Update system
sudo yum update -y   # Amazon Linux
# or
sudo apt update && sudo apt upgrade -y   # Ubuntu

# Install Java 17
sudo yum install java-17-amazon-corretto -y   # Amazon Linux
# or
sudo apt install openjdk-17-jdk -y   # Ubuntu

# Install Maven
sudo yum install maven -y   # Amazon Linux
# or
sudo apt install maven -y   # Ubuntu

# Install Git
sudo yum install git -y   # Amazon Linux
# or
sudo apt install git -y   # Ubuntu

# Verify installations
java -version
mvn -version
git --version
```

### Step 3: Clone Repository

```bash
# Clone your repo
git clone https://github.com/zzmarczz/e-commerceDemo.git
cd e-commerceDemo
```

### Step 4: Configure for AWS

```bash
# Run the quick fix script
./quick-fix-ip.sh
```

This will:
- Configure services to bind to all interfaces (0.0.0.0)
- Update CORS to allow your IP
- Set frontend to auto-detect API URL
- Configure API Gateway

### Step 5: Build

```bash
mvn clean install -DskipTests
```

This takes 2-3 minutes.

### Step 6: Start Services

```bash
# Start all services
./start-with-ui.sh
```

**Wait 60 seconds** for all services to start.

### Step 7: Verify

```bash
# Check if services are running
./check-services.sh

# Test API Gateway locally
curl http://localhost:8080/api/health

# Test Frontend locally
curl http://localhost:3000
```

### Step 8: Access from Browser

Get your EC2 public IP:
```bash
curl http://checkip.amazonaws.com
```

Open browser to:
```
http://YOUR_EC2_PUBLIC_IP:3000
```

Example: `http://3.10.224.202:3000`

---

## Troubleshooting

### Error: "NetworkError when attempting to fetch"

**Cause:** Security Group not configured or services not bound to 0.0.0.0

**Fix:**

1. Check Security Group has ports 3000 and 8080 open
2. Run: `./quick-fix-ip.sh`
3. Rebuild: `mvn clean install -DskipTests`
4. Restart: `./stop-all.sh && ./start-with-ui.sh`

### Error: "Connection Refused"

**Cause:** Services not running or not binding correctly

**Fix:**

```bash
# Check what's running
./check-services.sh

# View logs
tail -f logs/*.log

# Verify binding
netstat -tuln | grep -E ':(3000|8080|8081|8082|8083)'

# Should show 0.0.0.0:PORT or :::PORT (IPv6)
```

### Error: "Timeout"

**Cause:** Security Group blocking

**Fix:**

1. AWS Console → EC2 → Security Groups
2. Edit Inbound Rules
3. Ensure ports 3000, 8080 are open to 0.0.0.0/0

### Services Start But Can't Access

**Check firewall:**

```bash
# Amazon Linux / CentOS
sudo firewall-cmd --list-all

# Ubuntu
sudo ufw status

# If firewall is active, open ports
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --permanent --add-port=8080-8083/tcp
sudo firewall-cmd --reload

# Or for Ubuntu
sudo ufw allow 3000,8080:8083/tcp
```

---

## Running as a Service (Production)

### Create systemd services

**Frontend Service:**

```bash
sudo nano /etc/systemd/system/ecommerce-frontend.service
```

```ini
[Unit]
Description=E-Commerce Frontend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/e-commerceDemo
ExecStart=/usr/bin/java -jar /home/ec2-user/e-commerceDemo/frontend/target/frontend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Create similar services for each microservice, then:

```bash
sudo systemctl daemon-reload
sudo systemctl enable ecommerce-frontend
sudo systemctl start ecommerce-frontend
sudo systemctl status ecommerce-frontend
```

---

## Performance Optimization

### Increase JVM Memory

Edit startup scripts or systemd services:

```bash
java -Xms512m -Xmx1024m -jar service.jar
```

### Use Production Profile

```properties
# In application.properties
spring.profiles.active=production
```

---

## Monitoring

### Check Service Status

```bash
./check-services.sh
```

### View Logs

```bash
# Real-time logs
tail -f logs/*.log

# Specific service
tail -f logs/api-gateway.log

# Last 100 lines
tail -n 100 logs/frontend.log
```

### Monitor Resources

```bash
# CPU and memory
top

# Disk space
df -h

# Network
netstat -tuln | grep LISTEN
```

---

## Security Best Practices

### 1. Restrict Security Group

Instead of `0.0.0.0/0`, use your specific IP:

```
Source: YOUR_IP/32
```

### 2. Use HTTPS (Production)

Install nginx as reverse proxy with SSL:

```bash
sudo yum install nginx certbot -y

# Get SSL certificate
sudo certbot certonly --standalone -d your-domain.com

# Configure nginx to proxy to port 3000
```

### 3. Add Authentication

Consider adding Spring Security to the API Gateway.

### 4. Use RDS Instead of H2

For production, replace H2 with AWS RDS (PostgreSQL/MySQL).

---

## Auto-Start on Boot

### Using systemd

Create service files as shown above, then:

```bash
sudo systemctl enable ecommerce-frontend
sudo systemctl enable ecommerce-gateway
# etc.
```

### Using crontab

```bash
crontab -e
```

Add:
```
@reboot sleep 60 && cd /home/ec2-user/e-commerceDemo && ./start-with-ui.sh
```

---

## Backing Up

### Backup script

```bash
#!/bin/bash
tar -czf ecommerce-backup-$(date +%Y%m%d).tar.gz \
  --exclude='target' \
  --exclude='logs' \
  --exclude='.git' \
  e-commerceDemo/

# Upload to S3
aws s3 cp ecommerce-backup-*.tar.gz s3://your-bucket/backups/
```

---

## Cost Optimization

### Use Elastic IP

To keep the same IP even if you stop/start the instance:

1. AWS Console → EC2 → Elastic IPs
2. Allocate new address
3. Associate with your instance

**Note:** Elastic IPs are free when attached to running instances.

### Stop When Not Needed

```bash
# From your local machine
aws ec2 stop-instances --instance-ids i-1234567890abcdef0

# Start again
aws ec2 start-instances --instance-ids i-1234567890abcdef0
```

---

## Load Testing

Once running, test with load generator:

```bash
# On EC2 instance
./start-with-load.sh

# Access at http://YOUR_IP:3000
# Load stats at http://YOUR_IP:9090/stats
```

---

## Complete Deployment Checklist

✅ EC2 instance running (t2.medium+)  
✅ Security Group configured (ports 3000, 8080)  
✅ Java 17 installed  
✅ Maven installed  
✅ Repository cloned  
✅ ./quick-fix-ip.sh executed  
✅ mvn clean install completed  
✅ ./start-with-ui.sh executed  
✅ Services running (check with ./check-services.sh)  
✅ Can access http://YOUR_IP:3000  
✅ Products load successfully  
✅ Can add to cart and checkout  

---

## Quick Commands Reference

```bash
# Check service health
./check-services.sh

# View all logs
tail -f logs/*.log

# Restart everything
./stop-all.sh && ./start-with-ui.sh

# Check if ports are open
netstat -tuln | grep -E ':(3000|8080)'

# Get public IP
curl http://checkip.amazonaws.com

# Test API locally
curl http://localhost:8080/api/health

# Test from outside
curl http://YOUR_PUBLIC_IP:8080/api/health
```

---

## Getting Help

If issues persist:

1. Run: `./check-services.sh`
2. Check: `tail -f logs/api-gateway.log`
3. Verify: Security Group allows ports 3000, 8080
4. Test: `curl http://localhost:8080/api/health`
5. Check: `netstat -tuln | grep 8080`

Most common issue: Security Group not configured correctly.

**Quick fix:** Run `./quick-fix-ip.sh` and rebuild.


