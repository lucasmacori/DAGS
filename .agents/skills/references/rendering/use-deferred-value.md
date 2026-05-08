# REA-RND-02: Defer Expensive Derived Renders

Use `useDeferredValue()` when a fast-changing input drives an expensive render or computation. Keep the input immediate and let React update the heavy result slightly later.

## **Why This Matters:**

- **Input Latency:** Typing and pointer interactions stay responsive.
- **Controlled Work:** Expensive filtering or rendering moves off the urgent path.

## **Incorrect:**

```typescript
const Search = ({ items }: { items: Item[] }) => {
  const [query, setQuery] = useState('');
  const filteredItems = items.filter((item) => fuzzyMatch(item, query));

  return (
    <>
      <input value={query} onChange={(event) => setQuery(event.target.value)} />
      <ResultsList items={filteredItems} />
    </>
  );
};
```

## **Correct:**

```typescript
const Search = ({ items }: { items: Item[] }) => {
  const [query, setQuery] = useState('');
  const deferredQuery = useDeferredValue(query);
  const filteredItems = useMemo(
    () => items.filter((item) => fuzzyMatch(item, deferredQuery)),
    [items, deferredQuery]
  );

  return (
    <>
      <input value={query} onChange={(event) => setQuery(event.target.value)} />
      <ResultsList items={filteredItems} />
    </>
  );
};
```

## Related Resources

- [useDeferredValue - react.dev](https://react.dev/reference/react/useDeferredValue)
