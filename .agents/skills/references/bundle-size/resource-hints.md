# REA-BND-02: Hint Critical Resources Early

Use React DOM resource hint APIs to tell the browser about critical connections, fonts, styles, and modules before they become blocking.

## **Why This Matters:**

- **Network Warm-Up:** `prefetchDNS()` and `preconnect()` reduce connection setup time.
- **Faster Delivery:** `preload()` and related APIs help the browser schedule critical assets sooner.

## **Incorrect:**

```typescript
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html>
      <body>{children}</body>
    </html>
  );
}
```

## **Correct:**

```typescript
import { preconnect, preload } from 'react-dom';

export default function RootLayout({ children }: { children: React.ReactNode }) {
  preconnect('https://api.example.com');
  preload('/fonts/inter.woff2', {
    as: 'font',
    type: 'font/woff2',
    crossOrigin: 'anonymous',
  });

  return (
    <html>
      <body>{children}</body>
    </html>
  );
}
```

## Related Resources

- [React DOM resource preloading APIs - react.dev](https://react.dev/reference/react-dom#resource-preloading-apis)
