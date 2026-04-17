# Nexora Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development](#local-development)
3. [Docker Deployment](#docker-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Cloud Deployment](#cloud-deployment)
6. [Configuration](#configuration)
7. [Monitoring](#monitoring)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Node.js**: v18 or higher
- **Docker**: v24 or higher
- **Docker Compose**: v2.20 or higher
- **Kubernetes**: v1.28+ (for K8s deployment)
- **PostgreSQL**: v15+
- **Redis**: v7+
- **Kafka**: v3.5+

### Required Tools
```bash
# Install Java
sudo apt install openjdk-17-jdk

# Install Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Install kubectl
curl -LO "https://dl.k8s.io/release/v1.28.0/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

## Local Development

### 1. Clone Repository
```bash
git clone https://github.com/yourorg/nexora.git
cd nexora
```

### 2. Setup Environment Variables
```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3. Start Infrastructure Services
```bash
# Start PostgreSQL, Redis, Kafka
docker-compose up -d postgres redis kafka

# Wait for services to be ready
docker-compose ps
```

### 4. Build and Run Backend
```bash
# Using Gradle
./gradlew build
./gradlew bootRun

# Or using Maven
./mvnw clean package
./mvnw spring-boot:run
```

### 5. Setup Frontend
```bash
cd frontend
npm install
npm run dev
```

### 6. Access Services
- Backend API: http://localhost:8080
- Frontend: http://localhost:3000
- Swagger UI: http://localhost:8080/swagger-ui.html

## Docker Deployment

### 1. Build Images
```bash
# Build backend
docker build -t nexora-backend:latest .

# Build frontend
cd frontend
docker build -t nexora-frontend:latest ..
```

### 2. Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### 3. Docker Compose Services
The compose file includes:
- `postgres` - PostgreSQL database
- `redis` - Redis cache
- `kafka` - Message broker
- `zookeeper` - Kafka Zookeeper
- `backend` - Spring Boot API
- `frontend` - React web app
- `nginx` - Reverse proxy
- `prometheus` - Metrics collection
- `grafana` - Metrics visualization

## Kubernetes Deployment

### 1. Prerequisites
```bash
# Install Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Start Minikube
minikube start --driver=docker

# Enable Ingress
minikube addons enable ingress
```

### 2. Apply Kubernetes Configurations
```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Apply secrets
kubectl apply -f k8s/ingress.yaml

# Apply deployments
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml

# Check deployment status
kubectl get pods -n nexora
```

### 3. Access Services
```bash
# Port forward to backend
kubectl port-forward -n nexora svc/nexora-backend 8080:80

# Get ingress IP
kubectl get ingress -n nexora
```

## Cloud Deployment

### AWS EKS Deployment

#### 1. Create EKS Cluster
```bash
# Install eksctl
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin

# Create cluster
eksctl create cluster \
  --name nexora-prod \
  --region af-south-1 \
  --nodegroup-name workers \
  --node-type t3.medium \
  --nodes 2
```

#### 2. Deploy to EKS
```bash
# Update kubeconfig
aws eks update-kubeconfig --name nexora-prod

# Apply configurations
kubectl apply -f k8s/
```

#### 3. Setup RDS (PostgreSQL)
```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier nexora-db \
  --db-instance-class db.t3.medium \
  --engine postgres \
  --master-username nexora \
  --master-user-password <password> \
  --allocated-storage 100
```

#### 4. Setup ElastiCache (Redis)
```bash
# Create Redis cluster
aws elasticache create-replication-group \
  --replication-group-id nexora-redis \
  --engine redis \
  --cache-node-type cache.t3.medium \
  --num-cache-nodes 2
```

#### 5. Setup MSK (Kafka)
```bash
# Create MSK cluster
aws kafka create-cluster \
  --cluster-name nexora-kafka \
  --broker-node-group-info "instanceType=kafka.m5.large,nodeCount=3" \
  --encryption-info "encryptionInTransit={clientAuthentication=TLS,enabled=true}"
```

### Google Cloud GKE Deployment

#### 1. Create GKE Cluster
```bash
# Install gcloud CLI
curl https://sdk.cloud.google.com | bash
gcloud init

# Create cluster
gcloud container clusters create nexora-prod \
  --zone=africa-south1-a \
  --num-nodes=3 \
  --machine-type=e2-medium
```

#### 2. Deploy to GKE
```bash
# Get credentials
gcloud container clusters get-credentials nexora-prod

# Apply configurations
kubectl apply -f k8s/
```

#### 3. Setup Cloud SQL
```bash
# Create SQL instance
gcloud sql instances create nexora-db \
  --database-version=POSTGRES_15 \
  --tier=db-custom-e2-2 \
  --region=africa-south1
```

### Azure AKS Deployment

#### 1. Create AKS Cluster
```bash
# Install az CLI
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# Create resource group
az group create --name nexora-rg --location=eastus

# Create AKS cluster
az aks create \
  --resource-group nexora-rg \
  --name nexora-prod \
  --node-count 3 \
  --enable-addons monitoring
```

#### 2. Deploy to AKS
```bash
# Get credentials
az aks get-credentials --resource-group nexora-rg --name nexora-prod

# Apply configurations
kubectl apply -f k8s/
```

## Configuration

### Environment Variables

#### Backend
```yaml
server:
  port: 8080

database:
  url: jdbc:postgresql://postgres:5432/nexora
  username: nexora
  password: ${DB_PASSWORD}

redis:
  host: redis
  port: 6379

kafka:
  bootstrap-servers: kafka:9092

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

cors:
  allowed-origins: http://localhost:3000,https://nexora.example.com
```

#### Frontend
```yaml
VITE_API_URL: http://localhost:8080/api/v1
VITE_WS_URL: ws://localhost:8080/ws
VITE_ENV: development
```

### Secrets Management
```bash
# Using Kubernetes secrets
kubectl create secret generic nexora-secrets \
  --from-literal=DB_PASSWORD='your-password' \
  --from-literal=JWT_SECRET='your-jwt-secret' \
  -n nexora

# Using AWS Secrets Manager
aws secretsmanager create-secret \
  --name nexora/prod/db \
  --secret-string '{"username":"nexora","password":"your-password"}'
```

## Monitoring

### Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'nexora-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']
```

### Grafana Dashboards
- Import from: `grafana/dashboards/`
- Default credentials: admin/admin

### Alerting
```yaml
# alerts.yml
groups:
  - name: nexora-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: High error rate detected
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Failed
```bash
# Check PostgreSQL logs
docker logs nexora-postgres-1

# Test connection
docker exec -it nexora-postgres-1 psql -U nexora -c "SELECT 1"
```

#### 2. Redis Connection Failed
```bash
# Check Redis
docker logs nexora-redis-1

# Test connection
docker exec -it nexora-redis-1 redis-cli ping
```

#### 3. Kafka Issues
```bash
# Check Kafka
docker logs nexora-kafka-1

# List topics
docker exec -it nexora-kafka-1 kafka-topics --list --bootstrap-server localhost:9092
```

#### 4. Backend Won't Start
```bash
# Check logs
kubectl logs -n nexora deployment/nexora-backend

# Describe pod
kubectl describe pod -n nexora <pod-name>
```

### Health Checks
```bash
# Backend health
curl http://localhost:8080/api/v1/health

# Database health
curl http://localhost:8080/api/v1/health/db

# Redis health
curl http://localhost:8080/api/v1/health/redis
```

### Performance Tuning
```bash
# JVM options for production
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# PostgreSQL tuning
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET work_mem = '16MB';
```

## Revenue Model

### Income Streams
1. **Commission**: 10-15% on marketplace transactions
2. **Delivery Fees**: KES 150-500 per order
3. **Ride Commission**: 15-20% per ride
4. **Subscription**: KES 499/month premium
5. **Advertising**: CPM-based pricing
6. **Creator Monetization**: Revenue share on content

### M-Pesa Integration
- STK Push for payments
- B2C for withdrawals
- C2B for deposits

## Security

### SSL/TLS Setup
```bash
# Generate certificates
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout tls.key -out tls.crt \
  -subj "/C=KE/ST=Nairobi/L=Nairobi/O=Nexora"

# Create secret
kubectl create secret tls nexora-tls \
  --cert=tls.crt --key=tls.key -n nexora
```

### Firewall Rules
```bash
# Allow necessary ports
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw allow 5432  # PostgreSQL
sudo ufw allow 6379  # Redis
sudo ufw enable
```

## Backup and Recovery

### Database Backup
```bash
# Backup PostgreSQL
docker exec -t nexora-postgres-1 pg_dump -U nexora nexora > backup.sql

# Restore
docker exec -i nexora-postgres-1 psql -U nexora nexora < backup.sql
```

### Automated Backups
```yaml
# cronjob.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: nexora-backup
  namespace: nexora
spec:
  schedule: "0 2 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:15
            command: ["/bin/sh", "-c", "pg_dump -U nexora nexora > /backups/nexora-$(date +%Y%m%d).sql"]
          restartPolicy: OnFailure
```
