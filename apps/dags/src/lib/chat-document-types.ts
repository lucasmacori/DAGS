export type UploadedChatDocument = {
  documentId: string
  filename: string
  mediaType: string
  characterCount: number
  preview: string
}

export type UploadChatDocumentsResponse = {
  documents: {
    document_id: string
    filename: string
    media_type: string
    character_count: number
    preview: string
  }[]
}

export function mapUploadedChatDocumentsResponse(
  payload: UploadChatDocumentsResponse,
): UploadedChatDocument[] {
  return payload.documents.map((document) => ({
    documentId: document.document_id,
    filename: document.filename,
    mediaType: document.media_type,
    characterCount: document.character_count,
    preview: document.preview,
  }))
}
