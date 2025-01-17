# frontend/Dockerfile
FROM node:18-alpine AS build
WORKDIR /app

# Install dependencies using npm
COPY package.json package-lock.json ./
RUN npm install

# Copy the rest of the frontend source code and build the application
COPY . .
RUN npm run build

# Use NGINX to serve the built files
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
