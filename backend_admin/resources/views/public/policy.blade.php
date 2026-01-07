<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{ $title }} - Only Care</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            line-height: 1.6;
            color: #333;
            background-color: #f8f9fa;
        }

        .header {
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            color: white;
            padding: 2rem 1rem;
            text-align: center;
        }

        .header h1 {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .header .brand {
            font-size: 1rem;
            opacity: 0.9;
            margin-bottom: 1rem;
        }

        .header .updated {
            font-size: 0.875rem;
            opacity: 0.7;
        }

        .container {
            max-width: 800px;
            margin: 0 auto;
            padding: 2rem 1rem;
        }

        .content {
            background: white;
            border-radius: 12px;
            padding: 2rem;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        .content h2 {
            font-size: 1.5rem;
            font-weight: 600;
            color: #1a1a2e;
            margin-top: 2rem;
            margin-bottom: 1rem;
            padding-bottom: 0.5rem;
            border-bottom: 2px solid #f0f0f0;
        }

        .content h2:first-child {
            margin-top: 0;
        }

        .content h3 {
            font-size: 1.25rem;
            font-weight: 600;
            color: #333;
            margin-top: 1.5rem;
            margin-bottom: 0.75rem;
        }

        .content p {
            margin-bottom: 1rem;
            color: #555;
        }

        .content ul {
            margin-bottom: 1rem;
            padding-left: 1.5rem;
        }

        .content li {
            margin-bottom: 0.5rem;
            color: #555;
        }

        .content strong {
            color: #333;
        }

        .footer {
            text-align: center;
            padding: 2rem 1rem;
            color: #666;
            font-size: 0.875rem;
        }

        .footer a {
            color: #1a1a2e;
            text-decoration: none;
        }

        .footer a:hover {
            text-decoration: underline;
        }

        .back-link {
            display: inline-flex;
            align-items: center;
            color: white;
            text-decoration: none;
            font-size: 0.875rem;
            opacity: 0.8;
            margin-bottom: 1rem;
        }

        .back-link:hover {
            opacity: 1;
        }

        .back-link svg {
            margin-right: 0.5rem;
        }

        @media (max-width: 640px) {
            .header h1 {
                font-size: 1.5rem;
            }

            .content {
                padding: 1.5rem;
            }

            .content h2 {
                font-size: 1.25rem;
            }
        }
    </style>
</head>
<body>
    <header class="header">
        <a href="/" class="back-link">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
            Back to Home
        </a>
        <div class="brand">Only Care</div>
        <h1>{{ $title }}</h1>
        <div class="updated">Last updated: {{ $updated_at }}</div>
    </header>

    <main class="container">
        <div class="content">
            {!! $content !!}
        </div>
    </main>

    <footer class="footer">
        <p>&copy; {{ date('Y') }} Only Care. All rights reserved.</p>
        <p style="margin-top: 0.5rem;">
            <a href="/privacy-policy">Privacy Policy</a> &middot;
            <a href="/terms-conditions">Terms & Conditions</a> &middot;
            <a href="/refund-policy">Refund Policy</a>
        </p>
        <p style="margin-top: 1rem;">
            Contact: <a href="mailto:onlycareapp000@gmail.com">onlycareapp000@gmail.com</a>
        </p>
    </footer>
</body>
</html>
