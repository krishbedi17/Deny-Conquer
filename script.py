import os

def list_java_files_with_code(start_path='.'):
    output_file = 'file_list.txt'
    with open(output_file, 'w', encoding='utf-8') as f:
        for root, _, files in os.walk(start_path):
            for file in files:
                if file.endswith('.java'):
                    file_path = os.path.relpath(os.path.join(root, file), start_path)
                    f.write(f'===== {file_path} =====\n')
                    try:
                        with open(os.path.join(root, file), 'r', encoding='utf-8') as java_file:
                            f.write(java_file.read() + '\n\n')
                    except Exception as e:
                        f.write(f'[Error reading file: {e}]\n\n')
    print(f"Java file list with code written to {output_file}")

if __name__ == "__main__":
    list_java_files_with_code('.')
