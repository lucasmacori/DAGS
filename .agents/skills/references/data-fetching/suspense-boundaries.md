# REA-DAT-02: Use Suspense to Stream Non-Critical Content

When only part of a page depends on slow data, keep the shell renderable and wrap the slow section in `Suspense` instead of blocking the entire page.

## **Why This Matters:**

- **Perceived Performance:** Users see layout, navigation, and stable content earlier.
- **Isolation:** Slow data only blocks the component that needs it.

## **Incorrect:**

```typescript
export default async function ProductPage() {
  const recommendations = await fetchRecommendations();

  return (
    <main>
      <ProductHero />
      <Recommendations items={recommendations} />
    </main>
  );
}
```

## **Correct:**

```typescript
import { Suspense } from 'react';

const RecommendationsSection = async () => {
  const recommendations = await fetchRecommendations();
  return <Recommendations items={recommendations} />;
};

export default function ProductPage() {
  return (
    <main>
      <ProductHero />
      <Suspense fallback={<RecommendationsSkeleton />}>
        <RecommendationsSection />
      </Suspense>
    </main>
  );
}
```

## Related Resources

- [React Suspense - react.dev](https://react.dev/reference/react/Suspense)
