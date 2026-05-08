# REA-RND-01: Use Transitions for Non-Urgent Updates

Wrap non-urgent state updates in `startTransition()` so user input and other urgent work stay responsive.

## **Why This Matters:**

- **Responsiveness:** React can prioritize urgent updates over expensive UI refreshes.
- **User Experience:** Frequent updates like filters, panels, or navigation feel less blocking.

## **Incorrect:**

```typescript
const ScrollTracker = () => {
  const [scrollY, setScrollY] = useState(0);

  useEffect(() => {
    const handleScroll = () => setScrollY(window.scrollY);

    window.addEventListener('scroll', handleScroll, { passive: true });

    return () => window.removeEventListener('scroll', handleScroll);
  }, []);
};
```

## **Correct:**

```typescript
import { startTransition } from 'react';

const ScrollTracker = () => {
  const [scrollY, setScrollY] = useState(0);

  useEffect(() => {
    const handleScroll = () => {
      startTransition(() => setScrollY(window.scrollY));
    };

    window.addEventListener('scroll', handleScroll, { passive: true });

    return () => window.removeEventListener('scroll', handleScroll);
  }, []);
};
```

## Related Resources

- [startTransition - react.dev](https://react.dev/reference/react/startTransition)
