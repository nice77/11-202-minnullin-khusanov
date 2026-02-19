from flask import Flask, request, jsonify
import pymorphy3

app = Flask(__name__)
morph = pymorphy3.MorphAnalyzer()


@app.route('/lemmatize', methods=['POST'])
def lemmatize():
    data = request.json
    tokens = data.get('tokens', [])

    result = {}
    for token in tokens:
        parsed = morph.parse(token)[0]  # берём первый (наиболее вероятный) разбор
        lemma = parsed.normal_form

        if lemma not in result:
            result[lemma] = []
        if token not in result[lemma]:
            result[lemma].append(token)

    return jsonify(result)


@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok'})


if __name__ == '__main__':
    print("Запуск сервера лемматизации на http://localhost:5000")
    app.run(host='localhost', port=5000)