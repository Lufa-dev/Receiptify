FROM node:20-alpine
WORKDIR /app
# Copy package files first for better caching
COPY package*.json ./

RUN npm install
# Copy the rest of the files
COPY . .
# Build the application manually
RUN npm run build
# Expose the port and use the server.js express server
EXPOSE 4200
CMD ["node", "server.js"]
