import pandas as pd

# Farklı encoding seçenekleriyle CSV dosyasını oku
try:
    train_df = pd.read_csv("Train.csv", encoding="utf-8")  # Önce UTF-8 ile dene
except UnicodeDecodeError:
    try:
        train_df = pd.read_csv("Train.csv", encoding="ISO-8859-9")  # Türkçe karakter desteği
    except UnicodeDecodeError:
        train_df = pd.read_csv("Train.csv", encoding="latin1")  # Latin-1 ile dene

# İlk birkaç satırı kontrol edelim
print(train_df.head())
