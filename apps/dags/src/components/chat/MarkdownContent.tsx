import Markdown, { type Components } from 'react-markdown'
import rehypeKatex from 'rehype-katex'
import remarkGfm from 'remark-gfm'
import remarkMath from 'remark-math'
import 'katex/dist/katex.min.css'

type MarkdownContentProps = {
  content: string
}

const markdownComponents: Components = {
  a({ node: _node, ...props }) {
    return <a {...props} target="_blank" rel="noreferrer" />
  },
  blockquote({ node: _node, ...props }) {
    return <blockquote className="chat-markdown__blockquote" {...props} />
  },
  code({ node: _node, className, ...props }) {
    return <code className={`chat-markdown__code ${className ?? ''}`.trim()} {...props} />
  },
  h1({ node: _node, ...props }) {
    return <h2 className="chat-markdown__heading" {...props} />
  },
  h2({ node: _node, ...props }) {
    return <h3 className="chat-markdown__heading" {...props} />
  },
  h3({ node: _node, ...props }) {
    return <h4 className="chat-markdown__heading" {...props} />
  },
  li({ node: _node, ...props }) {
    return <li className="chat-markdown__list-item" {...props} />
  },
  ol({ node: _node, ...props }) {
    return <ol className="chat-markdown__list" {...props} />
  },
  p({ node: _node, ...props }) {
    return <p className="chat-bubble__text" {...props} />
  },
  pre({ node: _node, ...props }) {
    return <pre className="chat-markdown__pre" {...props} />
  },
  table({ node: _node, ...props }) {
    return <table className="chat-markdown__table" {...props} />
  },
  ul({ node: _node, ...props }) {
    return <ul className="chat-markdown__list" {...props} />
  },
}

export function MarkdownContent({ content }: MarkdownContentProps) {
  return (
    <div className="chat-markdown">
      <Markdown
        components={markdownComponents}
        rehypePlugins={[rehypeKatex]}
        remarkPlugins={[remarkGfm, remarkMath]}
      >
        {content}
      </Markdown>
    </div>
  )
}
