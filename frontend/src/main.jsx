import React, { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import axios from 'axios'

const root = document.getElementById('react-root')
const api = root.dataset.apiUrl
const emptyForm = { tipus: '', jelentes: '' }

function Hulladektipusok() {
    const [rows, setRows] = useState([])
    const [form, setForm] = useState(emptyForm)
    const [selectedId, setSelectedId] = useState(null)
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(true)

    async function load() {
        const response = await axios.get(api)
        setRows(response.data)
    }

    useEffect(() => {
        load()
            .catch(() => setError('Nem sikerült betölteni a hulladéktípusokat.'))
            .finally(() => setBusy(false))
    }, [])

    function resetForm() {
        setForm(emptyForm)
        setSelectedId(null)
    }

    async function save(event) {
        event.preventDefault()
        setBusy(true)
        setError('')

        try {
            if (selectedId === null) {
                await axios.post(api, form)
            } else {
                await axios.put(`${api}/${selectedId}`, form)
            }
            resetForm()
        } catch {
            setError('A mentést nem sikerült visszaigazolni.')
            setBusy(false)
            return
        }

        await load()
            .catch(() => setError('A mentés sikerült. A friss listához töltsd újra az oldalt.'))
            .finally(() => setBusy(false))
    }

    async function remove(id) {
        if (!window.confirm('Törlöd ezt a hulladéktípust?')) return

        setBusy(true)
        setError('')

        try {
            await axios.delete(`${api}/${id}`)
            if (selectedId === id) resetForm()
        } catch {
            setError('A törlést nem sikerült visszaigazolni.')
            setBusy(false)
            return
        }

        await load()
            .catch(() => setError('A törlés sikerült. A friss listához töltsd újra az oldalt.'))
            .finally(() => setBusy(false))
    }

    return (
        <>
            {error && <div className="alert alert-danger" role="alert">{error}</div>}

            <form onSubmit={save} className="gc p-4 mb-4">
                <fieldset disabled={busy}>
                    <legend>
                        {selectedId === null ? 'Új hulladéktípus' : 'Hulladéktípus módosítása'}
                    </legend>

                    <label htmlFor="tipus" className="form-label">Rövidítés</label>
                    <input id="tipus" className="form-control mb-3" required maxLength={30}
                           value={form.tipus}
                           onChange={event => setForm({ ...form, tipus: event.target.value })} />

                    <label htmlFor="jelentes" className="form-label">Leírás</label>
                    <textarea id="jelentes" className="form-control mb-3" required maxLength={255}
                              value={form.jelentes}
                              onChange={event => setForm({ ...form, jelentes: event.target.value })} />

                    <button type="submit" className="btn btn-primary me-2">Mentés</button>
                    {selectedId !== null &&
                        <button type="button" className="btn btn-outline-light" onClick={resetForm}>
                            Mégse
                        </button>}
                </fieldset>
            </form>

            {busy && <p role="status">Feldolgozás…</p>}
            {!busy && !error && rows.length === 0 && <p>Még nincs hulladéktípus.</p>}

            {rows.length > 0 &&
                <div className="gc p-3 table-responsive">
                    <table className="db-table">
                        <thead>
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Rövidítés</th>
                            <th scope="col">Leírás</th>
                            <th scope="col">Műveletek</th>
                        </tr>
                        </thead>
                        <tbody>
                        {rows.map(row =>
                            <tr key={row.id}>
                                <td>{row.id}</td>
                                <td>{row.tipus}</td>
                                <td>{row.jelentes}</td>
                                <td>
                                    <button type="button" className="btn btn-sm btn-outline-light me-2"
                                            disabled={busy} onClick={() => {
                                        setSelectedId(row.id)
                                        setForm({ tipus: row.tipus, jelentes: row.jelentes })
                                    }}>
                                        Módosítás
                                    </button>
                                    <button type="button" className="btn btn-sm btn-outline-danger"
                                            disabled={busy} onClick={() => remove(row.id)}>
                                        Törlés
                                    </button>
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>}
        </>
    )
}

createRoot(root).render(<Hulladektipusok />)