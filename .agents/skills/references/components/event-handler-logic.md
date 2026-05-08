# REA-CMP-01: Keep Interaction Logic in Event Handlers

If a side effect happens because a user clicked, submitted, or changed something, run that effect in the corresponding handler instead of modeling it as state plus `useEffect()`.

## **Why This Matters:**

- **Correctness:** Effects re-run when dependencies change, which can duplicate user actions.
- **Readability:** Handlers keep the intent tied to the interaction that triggered it.

## **Incorrect:**

```typescript
const RegistrationButton = () => {
  const [submitted, setSubmitted] = useState(false);
  const theme = useTheme();

  useEffect(() => {
    if (submitted) {
      registerUser();
      showToast('Registered', theme);
    }
  }, [submitted, theme]);

  return <button onClick={() => setSubmitted(true)}>Submit</button>;
};
```

## **Correct:**

```typescript
const RegistrationButton = () => {
  const theme = useTheme();

  const handleSubmit = () => {
    registerUser();
    showToast('Registered', theme);
  };

  return <button onClick={handleSubmit}>Submit</button>;
};
```

## Related Resources

- [You Might Not Need an Effect - react.dev](https://react.dev/learn/you-might-not-need-an-effect)
