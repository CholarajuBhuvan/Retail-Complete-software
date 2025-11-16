# Render Deployment Guide

This guide will help you deploy the Billing Software to Render.

## Prerequisites
- A Render account (sign up at https://render.com)
- Your GitHub repository connected to Render

## Deployment Steps

### Step 1: Create a Render Account
1. Go to https://render.com
2. Sign up with GitHub (this will make deployment easier)
3. Authorize Render to access your repositories

### Step 2: Deploy the Application

#### Option A: Using render.yaml (Recommended)
1. Log in to Render Dashboard
2. Click "New" → "Blueprint"
3. Connect your GitHub repository: `CholarajuBhuvan/Retail-Complete-software`
4. Render will automatically detect the `render.yaml` file
5. Click "Apply" to create all services

#### Option B: Manual Deployment
1. **Create PostgreSQL Database:**
   - Click "New" → "PostgreSQL"
   - Name: `billing-db`
   - Database: `billingdb`
   - User: `billing`
   - Plan: Free
   - Click "Create Database"

2. **Create Web Service:**
   - Click "New" → "Web Service"
   - Connect repository: `CholarajuBhuvan/Retail-Complete-software`
   - Name: `billing-software`
   - Environment: Docker
   - Dockerfile Path: `./Dockerfile.render`
   - Plan: Free
   - Add Environment Variables:
     ```
     SPRING_PROFILES_ACTIVE=prod
     SPRING_DATASOURCE_URL=[Copy from Database Internal Connection String]
     SPRING_DATASOURCE_USERNAME=billing
     SPRING_DATASOURCE_PASSWORD=[Copy from Database]
     SPRING_JPA_HIBERNATE_DDL_AUTO=none
     SPRING_FLYWAY_ENABLED=true
     ```
   - Click "Create Web Service"

### Step 3: Configure Environment Variables

After creating the database, you'll get connection details:
- **Internal Database URL**: Use this for `SPRING_DATASOURCE_URL`
- **Username**: billing
- **Password**: Copy from Render dashboard

Add these to your web service's environment variables.

### Step 4: Wait for Deployment

The first deployment takes 10-15 minutes because:
- Render builds the Docker image
- Maven downloads all dependencies
- Flyway runs database migrations

Watch the logs in the Render dashboard to track progress.

### Step 5: Access Your Application

Once deployed, Render provides a URL like:
```
https://billing-software.onrender.com
```

Default login credentials:
- **Manager**: manager / manager123
- **Admin**: admin / admin123
- **Cashier**: cashier / cashier123
- **Employee**: employee / employee123

## Important Notes

### Free Tier Limitations
- Services spin down after 15 minutes of inactivity
- First request after spin down takes 30-60 seconds
- Database has 90-day data retention

### Custom Domain (Optional)
1. Go to your web service settings
2. Click "Custom Domain"
3. Add your domain and configure DNS

### Troubleshooting

#### Build Failures
- Check Render logs for Maven errors
- Ensure `Dockerfile.render` is at project root
- Verify all dependencies are in `pom.xml`

#### Database Connection Issues
- Verify `SPRING_DATASOURCE_URL` uses internal database URL
- Check username and password match
- Ensure database is running (green status in dashboard)

#### Application Won't Start
- Check logs for Java errors
- Verify `SPRING_PROFILES_ACTIVE=prod`
- Ensure Flyway migrations completed successfully

#### 502 Bad Gateway
- Service might be spinning up (wait 60 seconds)
- Check if PORT environment variable is set
- Verify application is listening on `0.0.0.0:$PORT`

### Monitoring

Render provides:
- Real-time logs
- Metrics (CPU, Memory, Bandwidth)
- Deploy history
- Health checks

### Updating the Application

Push changes to GitHub:
```bash
git add .
git commit -m "Update application"
git push origin main
```

Render automatically redeploys on every push to `main` branch.

### Scaling

To upgrade from Free tier:
1. Go to web service settings
2. Choose a paid plan (starts at $7/month)
3. Benefits: No spin down, better performance, more resources

## Database Backup

Free tier includes automatic backups. To restore:
1. Go to database settings
2. Click "Backups" tab
3. Select a backup point
4. Click "Restore"

## Cost Estimate

- **Free Tier**: $0/month
  - PostgreSQL: 256 MB RAM, 1 GB storage
  - Web Service: Spins down after inactivity
  
- **Paid Tier**: $7-25/month
  - PostgreSQL: Starts at $7/month (256 MB RAM)
  - Web Service: Starts at $7/month (512 MB RAM)
  - No spin down, better performance

## Support

- Render Docs: https://render.com/docs
- Community: https://community.render.com
- Status Page: https://status.render.com
