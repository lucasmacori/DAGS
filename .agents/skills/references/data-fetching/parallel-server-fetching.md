# REA-DAT-01: Compose Server Components to Fetch in Parallel

React Server Components execute sequentially inside a single async tree. Break independent async work into sibling server components so requests can start at the same time.

## **Why This Matters:**

- **Latency:** Top-level sequential `await` calls create server-side waterfalls.
- **Scalability:** Component composition keeps fetch ownership local while preserving parallelism.

## **Incorrect:**

```typescript
export default async function Page() {
  const header = await fetchHeader();

  return (
    <div>
      <header>{header.title}</header>
      <Sidebar />
    </div>
  );
}

const Sidebar = async () => {
  const items = await fetchSidebarItems();
  return <nav>{items.map((item) => <span key={item.id}>{item.label}</span>)}</nav>;
};
```

## **Correct:**

```typescript
const Header = async () => {
  const header = await fetchHeader();
  return <header>{header.title}</header>;
};

const Sidebar = async () => {
  const items = await fetchSidebarItems();
  return <nav>{items.map((item) => <span key={item.id}>{item.label}</span>)}</nav>;
};

export default function Page() {
  return (
    <div>
      <Header />
      <Sidebar />
    </div>
  );
}
```

## Related Resources

- [React Suspense - react.dev](https://react.dev/reference/react/Suspense)
