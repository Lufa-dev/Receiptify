const express = require('express');
const path = require('path');
const app = express();

// Define the path to your static files
const staticPath = path.join(__dirname, 'dist/frontend/browser');

// Serve static files
app.use(express.static(staticPath));

// Handle all other routes by serving index.html
// No wildcard pattern that could trigger path-to-regexp
app.use((req, res) => {
  res.sendFile(path.join(staticPath, 'index.html'));
});

// Start the app
const PORT = process.env.PORT || 4200;
app.listen(PORT, () => {
});
