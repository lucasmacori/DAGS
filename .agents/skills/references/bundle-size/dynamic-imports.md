# REA-BND-01: Lazy-Load Heavy Components

Use dynamic imports for components that are expensive and not needed for the initial render, such as editors, charts, maps, or rarely opened panels.

## **Why This Matters:**

- **Bundle Size:** Keeps heavy code out of the initial client bundle.
- **Interactivity:** Reduces the amount of JavaScript competing with first render.

## **Incorrect:**

```typescript
import { AccountChart } from './account-chart';

const Dashboard = () => {
  return <AccountChart />;
};
```

## **Correct:**

```typescript
import dynamic from 'next/dynamic';

const AccountChart = dynamic(
  () => import('./account-chart').then((module) => module.AccountChart),
  { ssr: false }
);

const Dashboard = () => {
  return <AccountChart />;
};
```

## Related Resources

- [next/dynamic - Next.js](https://nextjs.org/docs/app/guides/lazy-loading#importing-client-components)
