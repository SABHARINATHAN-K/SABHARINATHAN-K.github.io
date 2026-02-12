# FrontEnd

Multipage HTML/CSS/JS client for Career Planning.

## Run
```bash
cd "/home/sabhari/VS/CAREER PLANNING/CAREER PLANNING/FrontEnd"
./run.sh
```

Open:
- http://localhost:5500

## Pages
- `index.html` - landing page
- `pages/login.html`
- `pages/register.html`
- `pages/dashboard.html`
- `pages/goals.html`

## Features
- Role and career-track selection during registration
- Dashboard stats (planned/in-progress/completed)
- Career-based goal template suggestions
- Goal search and status filtering
- Quick edit/delete actions from table

## Structure
- `assets/css/styles.css` - global UI/UX styling
- `assets/js/config.js` - API base URL
- `assets/js/ui.js` - UI helper utilities
- `assets/js/api.js` - API + token helper
- `assets/js/*.js` - page-specific logic
