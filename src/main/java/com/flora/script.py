import json

# 假设你的文件名是 data.jsonl
input_file = 'D:\\Dev\\personal-projects\\Xlsx2Markdown\\src\\main\\java\\com\\flora\\ChineseEcomQA.json'
output_file = 'EcomQA_Knowledge.md'

with open(input_file, 'r', encoding='utf-8') as f, open(output_file, 'w', encoding='utf-8') as out:
    out.write("# 电商品牌知识库\n\n")
    
    for line in f:
        data = json.loads(line)
        
        # 提取问题：去除 ***query***： 和 ***答案***： 等干扰项
        question = data['prompt'].replace('***query***：', '').replace('***答案***：', '').strip()
        # 提取答案
        answer = data['gt'].strip()
        
        # 写入 Markdown 格式
        out.write(f"### 问题：{question}\n")
        out.write(f"**回答：** {answer}\n\n")
        out.write("---\n\n")

print(f"转换完成！请查看 {output_file}")