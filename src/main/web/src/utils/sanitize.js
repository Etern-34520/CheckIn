import sanitizeHtml from "sanitize-html";
import * as csstree from 'css-tree';

/**
 * 严格过滤 Mermaid 样式：只保留包含 #mm ID 选择器的规则和 @keyframes
 * @param {string} cssText - 原始 CSS 文本
 * @returns {string} 过滤后的 CSS
 */
function filterMermaidStyle(cssText) {
    let ast;
    try {
        // 解析 CSS 为 AST
        ast = csstree.parse(cssText, {
            positions: true,   // 可选，便于调试
            onParseError: (error) => {
                // 忽略解析错误，继续尝试
                console.warn('CSS parse error:', error.message);
            }
        });
    } catch (e) {
        // 解析失败返回空字符串
        return '';
    }

    /**
     * 判断一个规则（Rule）是否应该保留
     * 规则是一个 csstree 的 Rule 节点，结构为 {
     *   type: 'Rule',
     *   prelude: { type: 'SelectorList', children: [...] },
     *   block: { type: 'Block', children: [...] }
     * }
     */
    function shouldKeepRule(ruleNode) {
        // 遍历所有选择器（SelectorList 中的每个 Selector）
        let hasMmId = false;
        csstree.walk(ruleNode.prelude, (node) => {
            if (node.type === 'IdSelector' && node.name.startsWith('mm')) {
                // 注意：IdSelector.name 不包含 #，例如 #mm123 的 name 是 "mm123"
                hasMmId = true;
            }
        });
        return hasMmId;
    }

    /**
     * 递归遍历 AST，构建新的 AST 节点列表
     */
    function filter(node) {
        // 如果没有子节点，直接返回 null（表示删除）
        if (!node) return null;

        switch (node.type) {
            case 'StyleSheet':
                // 对于根节点，过滤其 children
                const newChildren = [];
                if (node.children) {
                    node.children.forEach(child => {
                        const filtered = filter(child);
                        if (filtered) newChildren.push(filtered);
                    });
                }
                // 返回新的 StyleSheet 节点，children 替换
                return {
                    ...node,
                    children: newChildren
                };

            case 'Atrule':
                // 处理 at-rule
                if (node.name === 'keyframes' || node.name === '-webkit-keyframes' || node.name === '-moz-keyframes') {
                    // 直接保留所有 keyframes
                    return node;
                } else if (node.name === 'media' || node.name === 'supports' || node.name === 'document') {
                    // 对于 media/supports 等，需要过滤其内部的子规则
                    if (node.block && node.block.children) {
                        const newBlockChildren = [];
                        node.block.children.forEach(child => {
                            const filtered = filter(child);
                            if (filtered) newBlockChildren.push(filtered);
                        });
                        if (newBlockChildren.length > 0) {
                            // 返回新的 Atrule，block 内 children 替换
                            return {
                                ...node,
                                block: {
                                    ...node.block,
                                    children: newBlockChildren
                                }
                            };
                        } else {
                            return null; // 内部无保留规则，丢弃整个 at-rule
                        }
                    } else {
                        return null;
                    }
                } else {
                    // 其他 at-rule（@import, @charset 等）直接丢弃
                    return null;
                }

            case 'Rule':
                // 普通样式规则
                if (shouldKeepRule(node)) {
                    return node;
                } else {
                    return null;
                }

            default:
                // 其他类型节点（如 Comment, Raw 等）可以保留或丢弃，根据需求
                // 这里选择丢弃，因为 Mermaid 生成的 AST 中不应有这些
                return null;
        }
    }

    // 执行过滤
    const filteredAst = filter(ast);
    if (!filteredAst) return '';

    // 将 AST 重新生成为 CSS 字符串
    try {
        return csstree.generate(filteredAst);
    } catch (e) {
        console.warn('CSS generation error:', e);
        return '';
    }
}

function preFilterStyles(html) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const styleNodes = doc.querySelectorAll('style');
    styleNodes.forEach(style => {
        const originalCss = style.textContent;
        style.textContent = filterMermaidStyle(originalCss); // 替换为过滤后的内容
    });
    return doc.documentElement.outerHTML;
}
const customSanitizeHtml = (dirtyHtml) => {
    dirtyHtml = preFilterStyles(dirtyHtml);
    return sanitizeHtml(dirtyHtml, {
        // 允许的标签白名单保持不变（确保 SVG 相关标签都在）
        allowedTags: [
            'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p', 'br', 'hr',
            'ul', 'ol', 'li', 'dl', 'dt', 'dd',
            'table', 'thead', 'tbody', 'tr', 'th', 'td',
            'blockquote', 'pre', 'code',
            'strong', 'em', 's', 'del', 'mark', 'kbd', 'sub', 'sup',
            'a', 'img',
            'div', 'span',
            '!--',
            'details', 'summary',
            'svg', 'g', 'path', 'circle', 'line', 'rect', 'text', 'marker',
            'defs', 'symbol', 'use', 'foreignobject', 'tspan', 'stop',
            'lineargradient', 'radialgradient', 'clippath', 'mask', 'pattern',
            'image', 'ellipse', 'polygon', 'polyline',
            'style',
            'iframe'
        ],
        allowVulnerableTags: true,
        // 关键修改：允许所有属性（但配合协议过滤和标签白名单，风险可控）
        allowedAttributes: {
            '*': ['*']
        },
        // 保留 URL 协议过滤，防止 javascript: 等恶意链接
        allowedSchemes: ['http', 'https', 'ftp', 'mailto', 'tel'],
        allowedSchemesByTag: {img: ['http', 'https', 'data']},
        allowedSchemesAppliedToAttributes: ['href', 'src', 'xlink:href'],

        // 完全放行 class
        allowedClasses: {'*': ['*']},

        // 禁用样式解析（浏览器环境必需）
        parseStyleAttributes: false,

        // 允许 HTML 注释
        allowComments: true,

        // 移除 script 标签
        transformTags: {
            'script': sanitizeHtml.simpleTransform('script', {}, {disallowedTagsMode: 'discard'}),
            '*': function (tagName, attribs) {
                for (let attr in attribs) {
                    if (attr.startsWith('on')) {
                        delete attribs[attr];
                    }
                }
                return {tagName, attribs};
            }
        }
    });
}

export default customSanitizeHtml;