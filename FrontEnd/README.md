# FrontEnd

Multipage HTML/CSS/JS client for Career Planning.

## Run
```bash
cd "/home/sabhari/VS/CAREER PLANNING/CAREER PLANNING SYSTEM/FrontEnd"
./run.sh
```

Open:
- http://localhost:5500

## Pages
- `index.html` - landing page
- `pages/register.html`
- `pages/login.html`
- `pages/dashboard.html`
- `pages/goals.html`
- `pages/goal-detail.html?id={goalId}`
- `pages/profile.html`
- `pages/analytics.html`

## Features
- Modern UI style aligned to provided Figma/TXT design direction
- Role + career-track onboarding
- Dashboard with recent goals, completion rate, template suggestions
- Goal workspace with modal create flow, templates, filter/search, and card layout
- Goal detail page for progress/status/notes updates
- Profile page for editable user profile
- Analytics page for status/category/priority insights

## Structure
- `assets/css/styles.css` - global styling and page components
- `assets/js/config.js` - API base URL
- `assets/js/ui.js` - UI helper utilities
- `assets/js/api.js` - API and token utilities
- `assets/js/*.js` - page-specific logic
