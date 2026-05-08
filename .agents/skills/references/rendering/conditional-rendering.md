# REA-RND-03: Prefer Explicit JSX Conditionals

Use ternaries or explicit boolean checks when a value can be `0`, `NaN`, or another falsy renderable value. Avoid relying on `&&` in those cases.

## **Why This Matters:**

- **Correctness:** `0 && <Badge />` renders `0`, which is usually not intended.
- **Clarity:** Explicit conditions communicate when nothing should render.

## **Incorrect:**

```typescript
const Badge = ({ count }: { count: number }) => {
  return <div>{count && <span>{count}</span>}</div>;
};
```

## **Correct:**

```typescript
const Badge = ({ count }: { count: number }) => {
  return <div>{count > 0 ? <span>{count}</span> : null}</div>;
};
```

## Related Resources

- [Conditional Rendering - react.dev](https://react.dev/learn/conditional-rendering)
